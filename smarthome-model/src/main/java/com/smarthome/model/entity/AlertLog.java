package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警日志
 */
@Data
@TableName("alert_log")
public class AlertLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 告警规则ID */
    private Long alertId;

    /** 设备ID */
    private Long deviceId;

    /** 设备名称（冗余） */
    private String deviceName;

    /** 告警值 */
    private String alertValue;

    /** 告警级别 */
    private Integer alertLevel;

    /** 告警消息 */
    private String alertMessage;

    /** 状态: 1=未处理 2=已处理 3=已忽略 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    /** 处理备注 */
    private String handleRemark;
}
