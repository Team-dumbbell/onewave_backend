package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(nullable = false, length = 120)
    private String word;

    @Column(nullable = false, columnDefinition = "text")
    private String meaning;

    @Column(columnDefinition = "text")
    private String example; // AI가 가사 문맥에 맞춰 생성해줄 예문

    @Column(name = "part_of_speech", length = 40)
    private String partOfSpeech;

    @Enumerated(EnumType.STRING) // DB에 문자열로 저장
    @Column(nullable = false, length = 20)
    private Language language;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Builder
    public Word(User user, Music music, String word, String meaning, String example, String partOfSpeech, Language language) {
        this.user = user;
        this.music = music;
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.partOfSpeech = partOfSpeech;
        this.language = language;
    }
}