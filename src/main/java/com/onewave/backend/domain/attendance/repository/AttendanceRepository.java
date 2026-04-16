package com.onewave.backend.domain.attendance.repository;

import com.onewave.backend.domain.attendance.entity.Attendance;
import com.onewave.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    List<Attendance> findAllByUserAndAttendanceDateBetween(User user, LocalDate startDate, LocalDate endDate);
}