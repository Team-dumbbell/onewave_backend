package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.music.service.LyricService;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.dto.WordResponse;
import com.onewave.backend.domain.word.entity.*;
import com.onewave.backend.domain.word.repository.MusicWordRepository;
import com.onewave.backend.domain.word.repository.UserWordRepository;
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
        Music music = musicRepository.findByLrclibId(lrclibId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        // 언어는 유저의 언어 설정 추후에 변경하기
        WordExtractionResponse response = aiService.extractWords(lyricsDto.getPlainLyrics(), Language.KOREAN);

        return response.words().stream()
                .map(item -> processWordExtraction(item, user, music))
                .toList();
    }

    /**
     * 개별 단어 추출 처리 (마스터 확인 -> 유저 연결 -> 노래 연결)
     */
    private Word processWordExtraction(WordExtractionResponse.WordItem item, User user, Music music) {
        String cleanWordText = item.word().trim();

        // 1. 단어 마스터 존재 확인 및 생성 (뜻 제외)
        Word word = wordRepository.findByWord(cleanWordText)
                .orElseGet(() -> createNewWordMaster(item));

        // 2. 유저 단어장 연결 (개인화된 뜻 저장)
        if (!userWordRepository.existsByUserIdAndWordId(user.getId(), word.getId())) {
            userWordRepository.save(UserWord.builder()
                    .user(user)
                    .word(word)
                    .meaning(item.meaning())
                    .build());
        }

        // 3. 노래 연결 정보 저장
        if (!musicWordRepository.existsByMusicAndWord(music, word)) {
            musicWordRepository.save(new MusicWord(music, word));
        }

        return word;
    }

    private Word createNewWordMaster(WordExtractionResponse.WordItem dto) {
        Word word = Word.builder()
                .word(dto.word().trim())
                .partOfSpeech(dto.partOfSpeech())
                .language(dto.language())
                .build();

        // 유의어 및 예문 수동 매핑 (편의 메서드 미사용 시)
        if (dto.synonyms() != null) {
            dto.synonyms().forEach(s -> word.getSynonyms().add(
                    Synonym.builder().synonym(s).word(word).build()));
        }

        if (dto.examples() != null && !dto.examples().isBlank()) {
            word.getExamples().add(
                    ExampleSentence.builder().sentence(dto.examples().trim()).word(word).build());
        }

        return wordRepository.save(word);
    }

    /**
     * 사용자의 단어장 조회
     */
    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByUserId(Long userId) {
        // 이제 조회의 중심은 UserWord입니다.
        return userWordRepository.findAllByUserId(userId).stream()
                .map(this::convertToWordResponse)
                .toList();
    }

    private WordResponse convertToWordResponse(UserWord userWord) {
        Word word = userWord.getWord();

        // 이 단어와 연결된 모든 노래 정보 추출 및 중복 제거
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
                .meaning(userWord.getMeaning()) // ✨ UserWord의 개인화된 뜻 사용
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
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserWord userWord = userWordRepository.findByUserIdAndWordId(user.getId(), wordId)
                .orElseThrow(() -> new RuntimeException("내 단어장에서 해당 단어를 찾을 수 없습니다."));

        userWordRepository.delete(userWord);
    }
}