package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备属性历史记录
 */
@Data
@TableName("device_property_log")
public class DevicePropertyLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 设备ID */
    private Long deviceId;

    /** 物模型标识符 */
    private String identifier;

    /** 属性值 */
    private String value;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
