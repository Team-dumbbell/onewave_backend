package com.onewave.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String googleSub;

    @Column
    private String displayName;

    public void updateEmail(String email) {
        if (email != null && !email.isBlank()) this.email = email.trim();
    }

    public void updateDisplayName(String displayName) {
        if (displayName != null && !displayName.isBlank()) this.displayName = displayName.trim();
    }

    public static User create(String email, String googleSub, String displayName) {
        User u = new User();
        u.email = (email == null ? "" : email.trim());
        u.googleSub = googleSub;
        u.displayName = (displayName == null ? null : displayName.trim());
        return u;
    }
}