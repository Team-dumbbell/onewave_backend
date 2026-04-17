package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.music.service.LyricService;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.dto.WordResponse;
import com.onewave.backend.domain.word.entity.Language;
import com.onewave.backend.domain.word.entity.Word;
import com.onewave.backend.domain.word.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        // 1. LyricService를 통해 데이터 확보 (이미 내부에서 DB 저장까지 완료됨)
        LyricsResponse lyricsDto = lyricService.getLyricsById(lrclibId);

        // 2. 단어와 연결하기 위해 DB에서 Music 엔티티를 가져옴 (반드시 존재함)
        Music music = musicRepository.findByLrclibId(lrclibId).orElseThrow();

        // 3. AI 호출 및 단어 저장
        WordExtractionResponse response = aiService.extractWords(lyricsDto.getPlainLyrics(), Language.KOREAN);
        User user = userRepository.findById(userId).orElseThrow();

        List<Word> words = response.words().stream()
                .map(item -> Word.builder()
                        .user(user)
                        .music(music)
                        .word(item.word())
                        .meaning(item.meaning())
                        .example(item.example())
                        .partOfSpeech(item.partOfSpeech())
                        .language(Language.KOREAN)
                        .build())
                .collect(Collectors.toList());

        return wordRepository.saveAll(words);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getWordsByUserId(Long userId) {
        return wordRepository.findByUserId(userId).stream()
                .map(word -> {
                    // Word 엔티티에 music 연관관계가 있다고 가정 (예: word.getMusic())
                    String title = (word.getMusic() != null) ? word.getMusic().getTrackName() : "Unknown";
                    String artist = (word.getMusic() != null) ? word.getMusic().getArtistName() : "Unknown";

                    return WordResponse.builder()
                            .id(word.getId())
                            .word(word.getWord())
                            .meaning(word.getMeaning())
                            .partOfSpeech(word.getPartOfSpeech())
                            .example(word.getExample())
                            .musicTitle(title)
                            .artist(artist)
                            .build();
                })
                .toList();
    }
}