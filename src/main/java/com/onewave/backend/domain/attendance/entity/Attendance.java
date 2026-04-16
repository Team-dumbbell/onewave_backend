package com.onewave.backend.domain.attendance.entity;

import com.onewave.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "attendance_date"})
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Attendance(User user, LocalDate attendanceDate, LocalDateTime checkedAt) {
        this.user = user;
        this.attendanceDate = attendanceDate;
        this.checkedAt = checkedAt;
    }

    public static Attendance create(User user, LocalDate attendanceDate, LocalDateTime checkedAt) {
        return new Attendance(user, attendanceDate, checkedAt);
    }
}