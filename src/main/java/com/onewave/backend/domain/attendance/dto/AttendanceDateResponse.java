package com.onewave.backend.domain.attendance.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendanceDateResponse {

    private final LocalDate date;

    public AttendanceDateResponse(LocalDate date) {
        this.date = date;
    }
}