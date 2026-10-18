package com.pomodoro.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.pomodoro.model.DailyStatRow;
import com.pomodoro.model.TodayStatRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatsMapper {

    TodayStatRow findTodayPomodoroStat(@Param("userId") Long userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    long countCompletedTasks(@Param("userId") Long userId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    List<DailyStatRow> findDailyPomodoroCounts(@Param("userId") Long userId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}
