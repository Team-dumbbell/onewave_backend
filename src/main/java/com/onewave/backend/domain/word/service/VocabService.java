package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.music.service.LyricService;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.dto.WordResponse;
import com.onewave.backend.domain.word.entity.ExampleSentence;
import com.onewave.backend.domain.word.entity.Language;
import com.onewave.backend.domain.word.entity.Synonym;
import com.onewave.backend.domain.word.entity.Word;
import com.onewave.backend.domain.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabService {

    private final AiService aiService; // 공식 SDK를 사용하는 서비스
    private final LyricService lyricService;
    private final WordRepository wordRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<Word> extractAndSaveWords(Long lrclibId, Long userId) {
        LyricsResponse lyricsDto = lyricService.getLyricsById(lrclibId);
        Music music = musicRepository.findByLrclibId(lrclibId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        WordExtractionResponse response = aiService.extractWords(lyricsDto.getPlainLyrics(), Language.KOREAN);

        return response.words().stream()
                .map(item -> wordRepository.findByWordAndUserId(item.word(), userId)
                        .map(existingWord -> updateExistingWord(existingWord, item)) // 기존 단어 업데이트
                        .orElseGet(() -> createNewWord(user, music, item)))          // 새 단어 생성
                .toList();
    }

    // 1. 기존 단어 업데이트 로직 분리
    private Word updateExistingWord(Word word, WordExtractionResponse.WordItem dto) {
        word.incrementFrequency();
        addExamples(word, dto.examples());
        return word;
    }

    // 2. 새 단어 생성 로직 분리
    private Word createNewWord(User user, Music music, WordExtractionResponse.WordItem dto) {
        Word newWord = Word.builder()
                .user(user)
                .music(music)
                .word(dto.word())
                .meaning(dto.meaning())
                .partOfSpeech(dto.partOfSpeech())
                .language(Language.KOREAN)
                .build();

        addSynonyms(newWord, dto.synonyms());
        addExamples(newWord, dto.examples());

        return wordRepository.save(newWord);
    }

    // 3. 연관 관계 추가 공통 메서드
    private void addSynonyms(Word word, List<String> synonyms) {
        if (synonyms != null) {
            synonyms.forEach(s -> word.getSynonyms().add(
                    Synonym.builder().synonym(s).word(word).build()));
        }
    }

    private void addExamples(Word word, List<String> examples) {
        if (examples != null) {
            examples.forEach(e -> word.getExamples().add(
                    ExampleSentence.builder().sentence(e).word(word).build()));
        }
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByUserId(Long userId) {
        return wordRepository.findByUserId(userId).stream()
                .map(word -> WordResponse.builder()
                        .id(word.getId())
                        .word(word.getWord())
                        .meaning(word.getMeaning())
                        .partOfSpeech(word.getPartOfSpeech())
                        .frequency(word.getFrequency())
                        // 리스트 데이터 매핑
                        .synonyms(word.getSynonyms().stream().map(Synonym::getSynonym).toList())
                        .examples(word.getExamples().stream().map(ExampleSentence::getSentence).toList())
                        .musicTitle(word.getMusic() != null ? word.getMusic().getTrackName() : "Unknown")
                        .artist(word.getMusic() != null ? word.getMusic().getArtistName() : "Unknown")
                        .build())
                .toList();
    }

    @Transactional
    public void deleteWord(Long wordId, String googleSub) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("해당 단어를 찾을 수 없습니다."));

        // 본인 단어인지 확인 (보안)
        if (!word.getUser().getGoogleSub().equals(googleSub)) {
            throw new RuntimeException("본인의 단어만 삭제할 수 있습니다.");
        }

        wordRepository.delete(word);
    }
}