package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
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
    private final WordRepository wordRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    @Transactional
    public void extractAndSaveWords(Long musicId, Long userId, Language language) {
        // 1. 가사 데이터 가져오기
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        // 2. AI 호출 및 결과 파싱 (AiService 내부에서 작성하신 프롬프트 사용)
        WordExtractionResponse response = aiService.extractWords(music.getContent(), language.name());

        // 3. 유저 정보 확보
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. 엔티티 변환 및 벌크 저장 시도
        List<Word> words = response.words().stream()
                .map(item -> Word.builder()
                        .user(user)
                        .music(music)
                        .word(item.word())
                        .meaning(item.meaning())
                        .example(item.example())
                        .partOfSpeech(item.partOfSpeech())
                        .language(language)
                        .build())
                .collect(Collectors.toList());

        wordRepository.saveAll(words);

        // 5. 유의어 저장 (필요 시 WordSynonym 엔티티 연관관계 설정)
        // 이 부분은 WordSynonym 엔티티 구조에 따라 추가 구현이 필요합니다.
    }
}