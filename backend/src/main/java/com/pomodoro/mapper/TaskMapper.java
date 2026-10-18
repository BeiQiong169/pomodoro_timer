package com.pomodoro.mapper;

import java.util.List;

import com.pomodoro.model.TaskRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskMapper {

    List<TaskRow> findAllByUserId(@Param("userId") Long userId);

    TaskRow findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int insert(TaskRow task);

    int update(TaskRow task);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
