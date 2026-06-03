package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert_rule")
public class AlertRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long alertId;

    /** 告警名称 */
    private String alertName;

    /** 设备ID */
    private Long deviceId;

    /** 物模型标识符 */
    private String modelIdentifier;

    /** 比较运算符 */
    private String operator;

    /** 阈值 */
    private String threshold;

    /** 告警级别: 1=提示 2=警告 3=严重 */
    private Integer alertLevel;

    /** 通知方式: wechat/sms/email */
    private String notifyType;

    /** 是否启用: 0=禁用 1=启用 */
    private Integer enable;
}
