package com.onewave.backend.domain.music.entity;

import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long lrclibId;
    private String trackName;
    private String artistName;
    @Column(columnDefinition = "TEXT")
    private String content; // 가사 전문

    @Builder
    public Music(Long lrclibId, String trackName, String artistName, String content) {
        this.lrclibId = lrclibId;
        this.trackName = trackName;
        this.artistName = artistName;
        this.content = content;
    }
}
