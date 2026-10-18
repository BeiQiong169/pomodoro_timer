package com.pomodoro.dto.stats;

import java.time.LocalDate;

public record DailyStatResponse(LocalDate date, long pomodoroCount) {
}
