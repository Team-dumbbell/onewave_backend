package com.onewave.backend.domain.word.repository;

import com.onewave.backend.domain.word.entity.UserWord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {
    @EntityGraph(attributePaths = {
            "word",
            "word.synonyms",
            "word.examples",
            "word.musicWords.music"
    })
    List<UserWord> findAllByUserId(Long userId);

    Optional<UserWord> findByUserIdAndWordId(Long id, Long wordId);

    boolean existsByUserIdAndWordId(Long id, Long id1);
}
