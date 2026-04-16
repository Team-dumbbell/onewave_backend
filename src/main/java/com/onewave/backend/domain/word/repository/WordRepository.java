package com.onewave.backend.domain.word.repository;

import com.onewave.backend.domain.word.entity.Word;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    // 특정 유저가 특정 곡에서 추출한 단어 목록 조회 시 사용
    List<Word> findByUserIdAndMusicId(Long userId, Long musicId);
    Optional<Word> findByWordAndUserId(String word, Long userId);

    @EntityGraph(attributePaths = {"synonyms", "examples", "music"})
    List<Word> findByUserId(Long userId);
}
