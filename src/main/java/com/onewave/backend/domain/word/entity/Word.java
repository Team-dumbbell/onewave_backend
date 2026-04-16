package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // 사용자의 모국어로 번역된 의미 (예: 한국어 사용자가 일본 노래를 들으면 한국어로 저장)
    @Column(nullable = false, columnDefinition = "text")
    private String meaning;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Synonym> synonyms = new HashSet<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExampleSentence> examples = new HashSet<>();

    @Column(name = "part_of_speech", length = 40)
    private String partOfSpeech;

    @Column(nullable = false)
    private int frequency = 1;

    // 노래의 원래 언어
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Builder
    public Word(User user, Music music, String word, String meaning, String partOfSpeech, Language language, Integer frequency) {
        this.user = user;
        this.music = music;
        this.word = word;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
        this.language = language;
        this.frequency = (frequency != null) ? frequency : 1;
    }

    // 빈도수 증가 편의 메서드
    public void incrementFrequency() {
        this.frequency++;
    }
}