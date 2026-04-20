package com.onewave.backend.controller;

import com.onewave.backend.domain.attendance.dto.AttendanceCheckResponse;
import com.onewave.backend.domain.attendance.dto.AttendanceDateResponse;
import com.onewave.backend.domain.attendance.service.AttendanceService;
import com.onewave.backend.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<AttendanceCheckResponse>> checkAttendance(Authentication authentication) {
        String googleSub = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.checkAttendance(googleSub)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<AttendanceDateResponse>>> getMonthlyAttendance(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        String googleSub = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getMonthlyAttendance(googleSub, year, month)));
    }
}
