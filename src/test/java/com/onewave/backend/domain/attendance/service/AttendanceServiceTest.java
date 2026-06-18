package com.onewave.backend.domain.attendance.service;

import com.onewave.backend.domain.attendance.repository.AttendanceRepository;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {

    @Test
    void monthlyAttendanceRejectsInvalidMonth() {
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        AttendanceService service = new AttendanceService(attendanceRepository, userRepository);

        when(userRepository.findByGoogleSub("sub"))
                .thenReturn(Optional.of(User.create("user@example.com", "sub", "User")));

        assertThatThrownBy(() -> service.getMonthlyAttendance("sub", 2026, 13))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("유효한 연도와 월을 입력해주세요.");
    }
}
