package com.onewave.backend.domain.music.repository;

import com.onewave.backend.domain.music.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByLrclibId(Long lrclibId);
}