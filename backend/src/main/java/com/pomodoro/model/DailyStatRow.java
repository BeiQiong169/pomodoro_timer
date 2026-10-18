package com.pomodoro.model;

import java.time.LocalDate;

public class DailyStatRow {

    private LocalDate statDate;
    private long pomodoroCount;

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public long getPomodoroCount() {
        return pomodoroCount;
    }

    public void setPomodoroCount(long pomodoroCount) {
        this.pomodoroCount = pomodoroCount;
    }
}
