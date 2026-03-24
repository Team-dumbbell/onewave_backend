package com.onewave.backend.domain.word.repository;

import com.onewave.backend.domain.word.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    // 특정 유저가 특정 곡에서 추출한 단어 목록 조회 시 사용
    List<Word> findByUserIdAndMusicId(Long userId, Long musicId);
    List<Word> findByUserId(Long userId);
}
