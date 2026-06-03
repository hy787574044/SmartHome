package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设备分组-设备关联
 */
@Data
@TableName("device_group_device")
public class DeviceGroupDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组ID */
    private Long groupId;

    /** 设备ID */
    private Long deviceId;
}
