package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_config")
public class NotificationConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long configId;

    /** 用户ID */
    private Long userId;

    /** 通知类型: wechat/email/sms */
    private String notifyType;

    /** 配置(JSON): webhook url/email addr/phone */
    private String config;

    /** 是否启用: 0=禁用 1=启用 */
    private Integer enable;

    /** 免打扰开始时间 HH:mm */
    private String quietStart;

    /** 免打扰结束时间 HH:mm */
    private String quietEnd;
}
