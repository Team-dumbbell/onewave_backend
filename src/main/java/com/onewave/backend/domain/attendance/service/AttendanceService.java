package com.onewave.backend.domain.attendance.service;

import com.onewave.backend.domain.attendance.dto.AttendanceCheckResponse;
import com.onewave.backend.domain.attendance.dto.AttendanceDateResponse;
import com.onewave.backend.domain.attendance.entity.Attendance;
import com.onewave.backend.domain.attendance.repository.AttendanceRepository;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceCheckResponse checkAttendance(String googleSub) {
        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUserAndAttendanceDate(user, today)) {
            return new AttendanceCheckResponse(false, "이미 오늘 출석했습니다.", today);
        }

        Attendance attendance = Attendance.create(user, today, LocalDateTime.now());
        attendanceRepository.save(attendance);

        return new AttendanceCheckResponse(true, "출석이 완료되었습니다.", today);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDateResponse> getMonthlyAttendance(String googleSub, int year, int month) {
        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return attendanceRepository.findAllByUserAndAttendanceDateBetween(user, startDate, endDate)
                .stream()
                .map(attendance -> new AttendanceDateResponse(attendance.getAttendanceDate()))
                .toList();
    }
}