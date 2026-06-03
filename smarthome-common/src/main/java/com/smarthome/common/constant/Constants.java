package com.smarthome.common.constant;

/**
 * 系统常量
 */
public class Constants {

    /** 设备在线 */
    public static final int DEVICE_ONLINE = 3;
    /** 设备离线 */
    public static final int DEVICE_OFFLINE = 4;

    /** 物模型类型 - 属性 */
    public static final int MODEL_TYPE_PROPERTY = 1;
    /** 物模型类型 - 功能 */
    public static final int MODEL_TYPE_FUNCTION = 2;
    /** 物模型类型 - 事件 */
    public static final int MODEL_TYPE_EVENT = 3;

    /** 场景类型 - 手动 */
    public static final int SCENE_TYPE_MANUAL = 1;
    /** 场景类型 - 自动 */
    public static final int SCENE_TYPE_AUTO = 2;

    /** 触发类型 - 设备 */
    public static final int TRIGGER_TYPE_DEVICE = 1;
    /** 触发类型 - 定时 */
    public static final int TRIGGER_TYPE_TIMER = 2;
    /** 触发类型 - 条件 */
    public static final int TRIGGER_TYPE_CONDITION = 3;

    /** 动作类型 - 设备控制 */
    public static final int ACTION_TYPE_DEVICE = 1;
    /** 动作类型 - 告警通知 */
    public static final int ACTION_TYPE_ALERT = 2;

    /** 告警级别 - 提示 */
    public static final int ALERT_LEVEL_INFO = 1;
    /** 告警级别 - 警告 */
    public static final int ALERT_LEVEL_WARNING = 2;
    /** 告警级别 - 严重 */
    public static final int ALERT_LEVEL_CRITICAL = 3;

    /** 告警状态 - 未处理 */
    public static final int ALERT_STATUS_PENDING = 1;
    /** 告警状态 - 已处理 */
    public static final int ALERT_STATUS_HANDLED = 2;
    /** 告警状态 - 已忽略 */
    public static final int ALERT_STATUS_IGNORED = 3;

    /** Redis key 前缀 */
    public static final String REDIS_DEVICE_STATUS = "smarthome:device:status:";
    public static final String REDIS_DEVICE_PROPERTIES = "smarthome:device:properties:";
    public static final String REDIS_SCENE_SILENT = "smarthome:scene:silent:";

    /** MQTT Topic 模板 */
    public static final String TOPIC_PROPERTY_POST = "/%s/%s/property/post";
    public static final String TOPIC_PROPERTY_GET = "/%s/%s/property/get";
    public static final String TOPIC_FUNCTION_POST = "/%s/%s/functions/post";
    public static final String TOPIC_EVENT_POST = "/%s/%s/events/post";
    public static final String TOPIC_STATUS = "/%s/%s/status";
}
