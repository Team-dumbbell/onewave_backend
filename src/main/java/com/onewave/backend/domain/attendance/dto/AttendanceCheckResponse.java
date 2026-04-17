package com.onewave.backend.domain.attendance.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendanceCheckResponse {

    private final boolean success;
    private final String message;
    private final LocalDate date;

    public AttendanceCheckResponse(boolean success, String message, LocalDate date) {
        this.success = success;
        this.message = message;
        this.date = date;
    }
}