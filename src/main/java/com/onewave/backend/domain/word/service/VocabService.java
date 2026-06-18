package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.music.service.LyricService;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.dto.WordResponse;
import com.onewave.backend.domain.word.entity.ExampleSentence;
import com.onewave.backend.domain.word.entity.Language;
import com.onewave.backend.domain.word.entity.MusicWord;
import com.onewave.backend.domain.word.entity.Synonym;
import com.onewave.backend.domain.word.entity.UserWord;
import com.onewave.backend.domain.word.entity.Word;
import com.onewave.backend.domain.word.repository.MusicWordRepository;
import com.onewave.backend.domain.word.repository.UserWordRepository;
import com.onewave.backend.domain.word.repository.WordRepository;
import com.onewave.backend.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabService {

    private final AiService aiService;
    private final LyricService lyricService;
    private final WordRepository wordRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;
    private final MusicWordRepository musicWordRepository;
    private final UserWordRepository userWordRepository;

    @Transactional
    public List<Word> extractAndSaveWords(Long lrclibId, Long userId) {
        LyricsResponse lyricsDto = lyricService.getLyricsById(lrclibId);
        Music music = musicRepository.findByLrclibId(lrclibId)
                .orElseThrow(() -> new EntityNotFoundException("음악 정보를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        WordExtractionResponse response = aiService.extractWords(lyricsDto.getPlainLyrics(), Language.KOREAN);
        if (response == null || response.words() == null) {
            return Collections.emptyList();
        }

        return response.words().stream()
                .filter(this::hasRequiredWord)
                .map(item -> processWordExtraction(item, user, music))
                .toList();
    }

    private Word processWordExtraction(WordExtractionResponse.WordItem item, User user, Music music) {
        String cleanWordText = item.word().trim();

        Word word = wordRepository.findByWord(cleanWordText)
                .orElseGet(() -> createNewWordMaster(item));

        if (!userWordRepository.existsByUserIdAndWordId(user.getId(), word.getId())) {
            userWordRepository.save(UserWord.builder()
                    .user(user)
                    .word(word)
                    .meaning(defaultText(item.meaning()))
                    .build());
        }

        if (!musicWordRepository.existsByMusicAndWord(music, word)) {
            musicWordRepository.save(new MusicWord(music, word));
        }

        return word;
    }

    private Word createNewWordMaster(WordExtractionResponse.WordItem dto) {
        Word word = Word.builder()
                .word(dto.word().trim())
                .partOfSpeech(dto.partOfSpeech())
                .language(dto.language() == null ? Language.KOREAN : dto.language())
                .build();

        if (dto.synonyms() != null) {
            dto.synonyms().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .forEach(s -> word.getSynonyms().add(
                            Synonym.builder().synonym(s).word(word).build()));
        }

        if (dto.examples() != null && !dto.examples().isBlank()) {
            word.getExamples().add(
                    ExampleSentence.builder().sentence(dto.examples().trim()).word(word).build());
        }

        return wordRepository.save(word);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByUserId(Long userId) {
        return userWordRepository.findAllByUserId(userId).stream()
                .map(this::convertToWordResponse)
                .toList();
    }

    private WordResponse convertToWordResponse(UserWord userWord) {
        Word word = userWord.getWord();

        List<WordResponse.MusicInfo> musicInfos = word.getMusicWords().stream()
                .map(mw -> WordResponse.MusicInfo.builder()
                        .title(mw.getMusic().getTrackName())
                        .artist(mw.getMusic().getArtistName())
                        .build())
                .distinct()
                .toList();

        return WordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .meaning(userWord.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .frequency(musicInfos.size())
                .synonyms(word.getSynonyms().stream().map(Synonym::getSynonym).toList())
                .examples(word.getExamples().stream().map(ExampleSentence::getSentence).toList())
                .musicList(musicInfos)
                .build();
    }

    @Transactional
    public void deleteWord(Long wordId, String googleSub) {
        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        UserWord userWord = userWordRepository.findByUserIdAndWordId(user.getId(), wordId)
                .orElseThrow(() -> new EntityNotFoundException("내 단어장에서 해당 단어를 찾을 수 없습니다."));

        userWordRepository.delete(userWord);
    }

    private boolean hasRequiredWord(WordExtractionResponse.WordItem item) {
        return item != null && item.word() != null && !item.word().isBlank();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }
}
