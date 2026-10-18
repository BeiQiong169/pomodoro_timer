package com.pomodoro.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pomodoro.dto.stats.DailyStatResponse;
import com.pomodoro.dto.stats.StatsSummaryResponse;
import com.pomodoro.mapper.StatsMapper;
import com.pomodoro.model.DailyStatRow;
import com.pomodoro.model.TodayStatRow;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public StatsSummaryResponse summary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();

        TodayStatRow todayStat = statsMapper.findTodayPomodoroStat(userId, todayStart, tomorrowStart);
        long completedTasks = statsMapper.countCompletedTasks(userId, todayStart, tomorrowStart);
        Map<LocalDate, Long> dailyCounts = statsMapper
                .findDailyPomodoroCounts(userId, weekStart, tomorrowStart).stream()
                .collect(Collectors.toMap(DailyStatRow::getStatDate, DailyStatRow::getPomodoroCount));

        List<DailyStatResponse> last7Days = new ArrayList<>(7);
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);
            last7Days.add(new DailyStatResponse(date, dailyCounts.getOrDefault(date, 0L)));
        }

        return new StatsSummaryResponse(
                todayStat.getPomodoroCount(),
                todayStat.getFocusMinutes(),
                completedTasks,
                last7Days);
    }
}
