package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자 (조회 최적화용 + 보안용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 트랙에서 나온 단어인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(nullable = false, length = 120)
    private String word;

    @Column(nullable = false, columnDefinition = "text")
    private String meaning;

    @Column(name = "part_of_speech", length = 40)
    private String partOfSpeech;

    @Column(nullable = false)
    private Integer frequency = 1;

    @Column(nullable = false, length = 20)
    private String language; // ENGLISH/JAPANESE/KOREAN (나중에 Enum 추천)

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;
}