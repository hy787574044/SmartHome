package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
