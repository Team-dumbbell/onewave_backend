package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_word",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "word_id"})
        }
)
@Getter
@NoArgsConstructor
public class UserWord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(nullable = false, columnDefinition = "text")
    private String meaning;

    private OffsetDateTime addedAt = OffsetDateTime.now();

    @Builder
    public UserWord(User user, Word word, String meaning) {
        this.user = user;
        this.meaning = meaning;
        this.word = word;
    }
}
