package com.onewave.backend.domain.word.repository;

import com.onewave.backend.domain.word.entity.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {

    Optional<UserWord> findByUserIdAndWordId(Long id, Long wordId);

    boolean existsByUserIdAndWordId(Long id, Long id1);

    Optional<UserWord> findAllByUserId(Long userId);
}
