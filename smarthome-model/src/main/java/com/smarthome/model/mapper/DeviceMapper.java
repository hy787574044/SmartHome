package com.smarthome.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthome.model.entity.Device;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
