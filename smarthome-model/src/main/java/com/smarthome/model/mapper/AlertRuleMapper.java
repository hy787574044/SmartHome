package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
}
