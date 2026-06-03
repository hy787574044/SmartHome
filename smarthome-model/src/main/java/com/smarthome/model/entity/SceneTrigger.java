package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 场景触发条件
 */
@Data
@TableName("scene_trigger")
public class SceneTrigger {

    @TableId(type = IdType.AUTO)
    private Long triggerId;

    /** 所属场景ID */
    private Long sceneId;

    /** 触发类型: 1=设备 2=定时 3=条件 */
    private Integer triggerType;

    /** 设备ID（设备触发时） */
    private Long deviceId;

    /** 物模型标识符 */
    private String modelIdentifier;

    /** 比较运算符: =, !=, >, <, >=, <= */
    private String operator;

    /** 触发值 */
    private String value;

    /** cron 表达式（定时触发时） */
    private String cronExpression;
}
