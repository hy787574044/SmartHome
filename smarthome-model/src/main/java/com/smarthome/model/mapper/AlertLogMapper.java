package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.AlertLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertLogMapper extends BaseMapper<AlertLog> {
}
