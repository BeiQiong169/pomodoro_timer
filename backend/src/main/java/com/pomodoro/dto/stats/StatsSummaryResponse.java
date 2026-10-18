package com.pomodoro.dto.stats;

import java.util.List;

public record StatsSummaryResponse(
        long todayPomodoroCount,
        long todayFocusMinutes,
        long todayCompletedTaskCount,
        List<DailyStatResponse> last7Days) {
}
