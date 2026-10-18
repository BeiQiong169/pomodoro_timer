package com.pomodoro.mapper;

import com.pomodoro.model.UserRow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    UserRow findByUsername(String username);

    int insert(UserRow user);
}
