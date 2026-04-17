package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Column(nullable = false, length = 120)
    private String word;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL)
    private List<MusicWord> musicWords = new ArrayList<>();


    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Synonym> synonyms = new HashSet<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExampleSentence> examples = new HashSet<>();

    @Column(name = "part_of_speech", length = 40)
    private String partOfSpeech;

    // 노래의 원래 언어
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Builder
    public Word(String word, String partOfSpeech, Language language, Integer frequency) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.language = language;
    }
}