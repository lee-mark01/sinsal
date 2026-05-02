package com.sinsal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class SinSalRequest {

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    /**
     * 출생 시각 (HH:mm 형식, 선택).
     * 프론트에서 시 토글 OFF → "00:00" 전송, ON → 실제 시각 전송.
     * null이거나 생략하면 시주를 계산하지 않음.
     */
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
             message = "출생 시각은 HH:mm 형식이어야 합니다. (예: 14:30)")
    private String birthTime;

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
}
