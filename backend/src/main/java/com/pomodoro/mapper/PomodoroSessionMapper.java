package com.pomodoro.mapper;

import com.pomodoro.model.PomodoroSessionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PomodoroSessionMapper {

    int insert(PomodoroSessionRow session);

    PomodoroSessionRow findByUserIdAndClientToken(@Param("userId") Long userId,
                                                  @Param("clientToken") String clientToken);
}
