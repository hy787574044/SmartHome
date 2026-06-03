package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
