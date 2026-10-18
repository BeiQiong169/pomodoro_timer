package com.pomodoro.model;

public class TodayStatRow {

    private long pomodoroCount;
    private long focusMinutes;

    public long getPomodoroCount() {
        return pomodoroCount;
    }

    public void setPomodoroCount(long pomodoroCount) {
        this.pomodoroCount = pomodoroCount;
    }

    public long getFocusMinutes() {
        return focusMinutes;
    }

    public void setFocusMinutes(long focusMinutes) {
        this.focusMinutes = focusMinutes;
    }
}
