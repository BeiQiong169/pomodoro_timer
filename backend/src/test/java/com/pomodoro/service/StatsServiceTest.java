package com.pomodoro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.pomodoro.dto.stats.StatsSummaryResponse;
import com.pomodoro.mapper.StatsMapper;
import com.pomodoro.model.DailyStatRow;
import com.pomodoro.model.TodayStatRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsMapper statsMapper;

    @InjectMocks
    private StatsService statsService;

    @Test
    void summaryFillsMissingDaysWithZeroInAscendingOrder() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();

        TodayStatRow todayStat = new TodayStatRow();
        todayStat.setPomodoroCount(3);
        todayStat.setFocusMinutes(75);
        when(statsMapper.findTodayPomodoroStat(1L, todayStart, tomorrowStart)).thenReturn(todayStat);
        when(statsMapper.countCompletedTasks(1L, todayStart, tomorrowStart)).thenReturn(2L);

        DailyStatRow twoDaysAgo = new DailyStatRow();
        twoDaysAgo.setStatDate(today.minusDays(2));
        twoDaysAgo.setPomodoroCount(4);
        when(statsMapper.findDailyPomodoroCounts(1L, weekStart, tomorrowStart))
                .thenReturn(List.of(twoDaysAgo));

        StatsSummaryResponse response = statsService.summary(1L);

        assertThat(response.todayPomodoroCount()).isEqualTo(3);
        assertThat(response.todayFocusMinutes()).isEqualTo(75);
        assertThat(response.todayCompletedTaskCount()).isEqualTo(2);
        assertThat(response.last7Days()).hasSize(7);
        assertThat(response.last7Days().get(0).date()).isEqualTo(today.minusDays(6));
        assertThat(response.last7Days().get(6).date()).isEqualTo(today);
        assertThat(response.last7Days().get(4).date()).isEqualTo(today.minusDays(2));
        assertThat(response.last7Days().get(4).pomodoroCount()).isEqualTo(4);
        assertThat(response.last7Days().stream().filter(day -> day.pomodoroCount() == 0).toList())
                .hasSize(6);
    }
}
