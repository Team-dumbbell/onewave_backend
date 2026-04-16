package com.onewave.backend.domain.word.repository;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.word.entity.MusicWord;
import com.onewave.backend.domain.word.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicWordRepository extends JpaRepository<MusicWord, Long> {

    Optional<MusicWord> findByMusicAndWord(Music music, Word word);

    boolean existsByMusicAndWord(Music music, Word word);

    long countByWordId(Long id);
}
