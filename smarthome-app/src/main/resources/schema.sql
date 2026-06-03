-- ============================================================
-- SmartHome Database Schema
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 用户表 sys_user
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `user_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `sex`         TINYINT      DEFAULT 2 COMMENT '性别: 0=男 1=女 2=未知',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=正常',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 家庭表 family
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family` (
    `family_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '家庭ID',
    `family_name` VARCHAR(64)  NOT NULL COMMENT '家庭名称',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者ID',
    `invite_code` VARCHAR(10)  DEFAULT NULL COMMENT '邀请码',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=正常',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- -----------------------------------------------------------
-- 3. 家庭成员表 family_member
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member` (
    `member_id` BIGINT      NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `family_id` BIGINT      NOT NULL COMMENT '家庭ID',
    `user_id`   BIGINT      NOT NULL COMMENT '用户ID',
    `role`      VARCHAR(20) DEFAULT 'member' COMMENT '角色: admin/member/guest',
    `join_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`member_id`),
    UNIQUE KEY `uk_family_user` (`family_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- -----------------------------------------------------------
-- 4. 房间表 room
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
    `room_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `room_name`   VARCHAR(64)  NOT NULL COMMENT '房间名称',
    `room_type`   VARCHAR(32)  DEFAULT NULL COMMENT '房间类型: living_room/bedroom/kitchen/bathroom/study/balcony',
    `floor`       INT          DEFAULT 1 COMMENT '楼层',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- -----------------------------------------------------------
-- 5. 产品模板表 product
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `product_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `product_name`   VARCHAR(128) NOT NULL COMMENT '产品名称',
    `category_id`    BIGINT       DEFAULT NULL COMMENT '产品分类ID',
    `device_type`    TINYINT      DEFAULT 1 COMMENT '设备类型: 1=直连设备 2=网关 3=监测设备',
    `network_method` VARCHAR(32)  DEFAULT 'wifi' COMMENT '联网方式: wifi/zigbee/ble/ethernet',
    `status`         TINYINT      DEFAULT 1 COMMENT '产品状态: 0=禁用 1=启用',
    `description`    VARCHAR(512) DEFAULT NULL COMMENT '产品描述',
    `create_by`      VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`         VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品模板表';

-- -----------------------------------------------------------
-- 6. 物模型表 things_model
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `things_model`;
CREATE TABLE `things_model` (
    `model_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `product_id`  BIGINT       NOT NULL COMMENT '所属产品ID',
    `model_name`  VARCHAR(64)  NOT NULL COMMENT '模型名称',
    `identifier`  VARCHAR(64)  NOT NULL COMMENT '标识符',
    `type`        TINYINT      DEFAULT 1 COMMENT '类型: 1=属性 2=功能 3=事件',
    `data_type`   VARCHAR(32)  DEFAULT 'string' COMMENT '数据类型: integer/decimal/string/bool/enum',
    `unit`        VARCHAR(32)  DEFAULT NULL COMMENT '单位',
    `readonly`    TINYINT      DEFAULT 0 COMMENT '是否只读: 0=否 1=是',
    `show_index`  TINYINT      DEFAULT 0 COMMENT '是否在首页展示: 0=否 1=是',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `specs`       TEXT         DEFAULT NULL COMMENT '规格JSON',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`model_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物模型表';

-- -----------------------------------------------------------
-- 7. 设备表 device
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
    `device_id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_name`       VARCHAR(128) NOT NULL COMMENT '设备名称',
    `product_id`        BIGINT       NOT NULL COMMENT '所属产品ID',
    `serial_number`     VARCHAR(64)  NOT NULL COMMENT '设备序列号',
    `device_type`       TINYINT      DEFAULT 1 COMMENT '设备类型: 1=直连 2=网关 3=监测',
    `status`            TINYINT      DEFAULT 1 COMMENT '设备状态: 1=未激活 2=禁用 3=在线 4=离线',
    `room_id`           BIGINT       DEFAULT NULL COMMENT '所属房间ID',
    `gw_serial_number`  VARCHAR(64)  DEFAULT NULL COMMENT '网关设备序列号',
    `slave_id`          INT          DEFAULT NULL COMMENT '子设备地址',
    `last_online_time`  DATETIME     DEFAULT NULL COMMENT '最后上线时间',
    `last_offline_time` DATETIME     DEFAULT NULL COMMENT '最后离线时间',
    `create_by`         VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`            VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_serial_number` (`serial_number`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- -----------------------------------------------------------
-- 8. 场景表 scene
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `scene`;
CREATE TABLE `scene` (
    `scene_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '场景ID',
    `scene_name`     VARCHAR(64)  NOT NULL COMMENT '场景名称',
    `scene_type`     TINYINT      DEFAULT 1 COMMENT '场景类型: 1=手动 2=自动',
    `enable`         TINYINT      DEFAULT 1 COMMENT '是否启用: 0=禁用 1=启用',
    `condition_type` TINYINT      DEFAULT 1 COMMENT '触发条件关系: 1=OR 2=AND',
    `execute_mode`   TINYINT      DEFAULT 1 COMMENT '执行模式: 1=串行 2=并行',
    `delay_seconds`  INT          DEFAULT 0 COMMENT '执行延迟（秒）',
    `silent_period`  INT          DEFAULT 0 COMMENT '静默期（分钟）',
    `icon`           VARCHAR(128) DEFAULT NULL COMMENT '场景图标',
    `create_by`      VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`         VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景表';

-- -----------------------------------------------------------
-- 9. 场景触发条件表 scene_trigger
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `scene_trigger`;
CREATE TABLE `scene_trigger` (
    `trigger_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '触发ID',
    `scene_id`         BIGINT       NOT NULL COMMENT '所属场景ID',
    `trigger_type`     TINYINT      DEFAULT 1 COMMENT '触发类型: 1=设备 2=定时 3=条件',
    `device_id`        BIGINT       DEFAULT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(64)  DEFAULT NULL COMMENT '物模型标识符',
    `operator`         VARCHAR(8)   DEFAULT NULL COMMENT '比较运算符',
    `value`            VARCHAR(128) DEFAULT NULL COMMENT '触发值',
    `cron_expression`  VARCHAR(64)  DEFAULT NULL COMMENT 'cron表达式',
    PRIMARY KEY (`trigger_id`),
    KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景触发条件表';

-- -----------------------------------------------------------
-- 10. 场景执行动作表 scene_action
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `scene_action`;
CREATE TABLE `scene_action` (
    `action_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '动作ID',
    `scene_id`         BIGINT       NOT NULL COMMENT '所属场景ID',
    `action_type`      TINYINT      DEFAULT 1 COMMENT '动作类型: 1=设备控制 2=告警通知',
    `device_id`        BIGINT       DEFAULT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(64)  DEFAULT NULL COMMENT '物模型标识符',
    `value`            VARCHAR(128) DEFAULT NULL COMMENT '动作值',
    `delay_seconds`    INT          DEFAULT 0 COMMENT '延迟执行（秒）',
    `sort_order`       INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`action_id`),
    KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景执行动作表';

-- -----------------------------------------------------------
-- 11. 场景执行日志表 scene_log
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `scene_log`;
CREATE TABLE `scene_log` (
    `log_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `scene_id`      BIGINT       NOT NULL COMMENT '场景ID',
    `trigger_info`  VARCHAR(512) DEFAULT NULL COMMENT '触发信息',
    `action_results` TEXT        DEFAULT NULL COMMENT '执行结果JSON',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态: 0=失败 1=成功 2=部分成功',
    `execute_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_scene_id` (`scene_id`),
    KEY `idx_execute_time` (`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景执行日志表';

-- -----------------------------------------------------------
-- 12. 告警规则表 alert_rule
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule` (
    `alert_id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `alert_name`       VARCHAR(128) NOT NULL COMMENT '告警名称',
    `device_id`        BIGINT       NOT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(64)  NOT NULL COMMENT '物模型标识符',
    `operator`         VARCHAR(8)   NOT NULL COMMENT '比较运算符',
    `threshold`        VARCHAR(128) NOT NULL COMMENT '阈值',
    `alert_level`      TINYINT      DEFAULT 1 COMMENT '告警级别: 1=提示 2=警告 3=严重',
    `notify_type`      VARCHAR(32)  DEFAULT 'wechat' COMMENT '通知方式',
    `enable`           TINYINT      DEFAULT 1 COMMENT '是否启用: 0=禁用 1=启用',
    `create_by`        VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`           VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`alert_id`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- -----------------------------------------------------------
-- 13. 告警日志表 alert_log
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `alert_log`;
CREATE TABLE `alert_log` (
    `log_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `alert_id`     BIGINT       NOT NULL COMMENT '告警规则ID',
    `device_id`    BIGINT       NOT NULL COMMENT '设备ID',
    `device_name`  VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    `alert_value`  VARCHAR(128) DEFAULT NULL COMMENT '告警值',
    `alert_level`  TINYINT      DEFAULT 1 COMMENT '告警级别',
    `alert_message` VARCHAR(512) DEFAULT NULL COMMENT '告警消息',
    `status`       TINYINT      DEFAULT 1 COMMENT '状态: 1=未处理 2=已处理 3=已忽略',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `handle_time`  DATETIME     DEFAULT NULL COMMENT '处理时间',
    `handle_remark` VARCHAR(256) DEFAULT NULL COMMENT '处理备注',
    PRIMARY KEY (`log_id`),
    KEY `idx_alert_id` (`alert_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警日志表';

-- -----------------------------------------------------------
-- 14. 设备属性历史记录表 device_property_log
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `device_property_log`;
CREATE TABLE `device_property_log` (
    `log_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id`  BIGINT       NOT NULL COMMENT '设备ID',
    `identifier` VARCHAR(64)  NOT NULL COMMENT '物模型标识符',
    `value`      VARCHAR(256) DEFAULT NULL COMMENT '属性值',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备属性历史记录表';

-- -----------------------------------------------------------
-- 15. 设备分组表 device_group
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `device_group`;
CREATE TABLE `device_group` (
    `group_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `group_name`  VARCHAR(64)  NOT NULL COMMENT '分组名称',
    `icon`        VARCHAR(128) DEFAULT NULL COMMENT '图标',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分组表';

-- -----------------------------------------------------------
-- 16. 设备分组-设备关联表 device_group_device
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `device_group_device`;
CREATE TABLE `device_group_device` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `group_id`  BIGINT NOT NULL COMMENT '分组ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_device` (`group_id`, `device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分组-设备关联表';

-- -----------------------------------------------------------
-- 17. 通知配置表 notification_config
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `notification_config`;
CREATE TABLE `notification_config` (
    `config_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `notify_type` VARCHAR(32)  NOT NULL COMMENT '通知类型: wechat/email/sms',
    `config`      VARCHAR(512) DEFAULT NULL COMMENT '配置JSON',
    `enable`      TINYINT      DEFAULT 1 COMMENT '是否启用: 0=禁用 1=启用',
    `quiet_start` VARCHAR(10)  DEFAULT NULL COMMENT '免打扰开始时间 HH:mm',
    `quiet_end`   VARCHAR(10)  DEFAULT NULL COMMENT '免打扰结束时间 HH:mm',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- -----------------------------------------------------------
-- 18. 操作日志表 operation_log
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `log_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`    BIGINT       DEFAULT NULL COMMENT '操作用户ID',
    `username`   VARCHAR(64)  DEFAULT NULL COMMENT '操作用户名',
    `module`     VARCHAR(32)  DEFAULT NULL COMMENT '操作模块',
    `operation`  VARCHAR(32)  DEFAULT NULL COMMENT '操作类型',
    `target`     VARCHAR(128) DEFAULT NULL COMMENT '操作目标',
    `detail`     VARCHAR(512) DEFAULT NULL COMMENT '操作内容',
    `ip`         VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    `status`     TINYINT      DEFAULT 1 COMMENT '操作状态: 0=失败 1=成功',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------------------
-- 19. 系统配置表 system_config
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    `config_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`   VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(512) DEFAULT NULL COMMENT '配置值',
    `config_name`  VARCHAR(128) DEFAULT NULL COMMENT '配置名称',
    `create_by`    VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`       VARCHAR(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Sample Data
-- ============================================================

-- -----------------------------------------------------------
-- 用户数据 (password: admin123 BCrypt hash)
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `sex`, `status`, `create_by`, `create_time`)
VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', NULL, 'admin@smarthome.com', '13800000001', 0, 1, 'system', '2024-01-01 00:00:00'),
(2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', NULL, 'zhangsan@smarthome.com', '13800000002', 0, 1, 'admin', '2024-01-15 10:00:00'),
(3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', NULL, 'lisi@smarthome.com', '13800000003', 1, 1, 'admin', '2024-02-01 14:30:00');

-- -----------------------------------------------------------
-- 家庭数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `family` (`family_id`, `family_name`, `creator_id`, `invite_code`, `status`, `create_by`, `create_time`)
VALUES
(1, '我的家', 1, 'A1B2C3', 1, 'admin', '2024-01-01 00:00:00'),
(2, '父母家', 1, 'D4E5F6', 1, 'admin', '2024-03-01 10:00:00');

-- -----------------------------------------------------------
-- 家庭成员数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `family_member` (`member_id`, `family_id`, `user_id`, `role`, `join_time`)
VALUES
(1, 1, 1, 'admin', '2024-01-01 00:00:00'),
(2, 1, 2, 'member', '2024-01-15 10:00:00'),
(3, 1, 3, 'member', '2024-02-01 14:30:00'),
(4, 2, 1, 'admin', '2024-03-01 10:00:00');

-- -----------------------------------------------------------
-- 房间数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `room` (`room_id`, `room_name`, `room_type`, `floor`, `sort_order`, `create_by`, `create_time`)
VALUES
(1, '客厅', 'living_room', 1, 1, 'admin', '2024-01-01 00:00:00'),
(2, '主卧', 'bedroom', 1, 2, 'admin', '2024-01-01 00:00:00'),
(3, '次卧', 'bedroom', 1, 3, 'admin', '2024-01-01 00:00:00'),
(4, '厨房', 'kitchen', 1, 4, 'admin', '2024-01-01 00:00:00'),
(5, '卫生间', 'bathroom', 1, 5, 'admin', '2024-01-01 00:00:00'),
(6, '书房', 'study', 1, 6, 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 产品模板数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `product` (`product_id`, `product_name`, `category_id`, `device_type`, `network_method`, `status`, `description`, `create_by`, `create_time`)
VALUES
(1, '智能灯', 1, 1, 'wifi', 1, '支持亮度和色温调节的智能LED灯', 'admin', '2024-01-01 00:00:00'),
(2, '温湿度传感器', 2, 3, 'zigbee', 1, '实时监测室内温度和湿度', 'admin', '2024-01-01 00:00:00'),
(3, '智能插座', 3, 1, 'wifi', 1, '支持远程开关和电量统计的智能插座', 'admin', '2024-01-01 00:00:00'),
(4, '智能窗帘', 4, 1, 'wifi', 1, '支持远程开合控制的智能窗帘电机', 'admin', '2024-01-01 00:00:00'),
(5, '烟雾报警器', 5, 3, 'zigbee', 1, '烟雾浓度检测与实时报警', 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 物模型数据 -- 智能灯 (product_id=1)
-- -----------------------------------------------------------
INSERT IGNORE INTO `things_model` (`model_id`, `product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`, `specs`, `create_by`, `create_time`)
VALUES
(1,  1, '开关',   'switch',     1, 'bool',    NULL, 0, 1, 1, '{"true":"开","false":"关"}', 'admin', '2024-01-01 00:00:00'),
(2,  1, '亮度',   'brightness', 1, 'integer', '%',  0, 1, 2, '{"min":0,"max":100,"step":1}', 'admin', '2024-01-01 00:00:00'),
(3,  1, '色温',   'color_temp', 1, 'integer', 'K',  0, 0, 3, '{"min":2700,"max":6500,"step":100}', 'admin', '2024-01-01 00:00:00'),
(4,  1, '颜色',   'color',      1, 'string',  NULL, 0, 0, 4, NULL, 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 物模型数据 -- 温湿度传感器 (product_id=2)
-- -----------------------------------------------------------
INSERT IGNORE INTO `things_model` (`model_id`, `product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`, `specs`, `create_by`, `create_time`)
VALUES
(5,  2, '温度',     'temperature',  1, 'decimal', '°C',  1, 1, 1, '{"min":-40,"max":80,"step":0.1}', 'admin', '2024-01-01 00:00:00'),
(6,  2, '湿度',     'humidity',     1, 'decimal', '%RH', 1, 1, 2, '{"min":0,"max":100,"step":0.1}', 'admin', '2024-01-01 00:00:00'),
(7,  2, '电池电量', 'battery',      1, 'integer', '%',   1, 0, 3, '{"min":0,"max":100}', 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 物模型数据 -- 智能插座 (product_id=3)
-- -----------------------------------------------------------
INSERT IGNORE INTO `things_model` (`model_id`, `product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`, `specs`, `create_by`, `create_time`)
VALUES
(8,  3, '开关',   'switch',      1, 'bool',    NULL, 0, 1, 1, '{"true":"开","false":"关"}', 'admin', '2024-01-01 00:00:00'),
(9,  3, '功率',   'power',       1, 'decimal', 'W',  1, 1, 2, '{"min":0,"max":3500,"step":0.1}', 'admin', '2024-01-01 00:00:00'),
(10, 3, '电量',   'electricity', 1, 'decimal', 'kWh',1, 0, 3, '{"min":0,"step":0.01}', 'admin', '2024-01-01 00:00:00'),
(11, 3, '电压',   'voltage',     1, 'decimal', 'V',  1, 0, 4, '{"min":0,"max":250,"step":0.1}', 'admin', '2024-01-01 00:00:00'),
(12, 3, '电流',   'current',     1, 'decimal', 'A',  1, 0, 5, '{"min":0,"max":16,"step":0.01}', 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 物模型数据 -- 智能窗帘 (product_id=4)
-- -----------------------------------------------------------
INSERT IGNORE INTO `things_model` (`model_id`, `product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`, `specs`, `create_by`, `create_time`)
VALUES
(13, 4, '窗帘状态', 'curtain_status', 1, 'enum',   NULL, 0, 1, 1, '{"0":"关闭","1":"打开","2":"半开"}', 'admin', '2024-01-01 00:00:00'),
(14, 4, '开合度',   'position',       1, 'integer', '%',  0, 1, 2, '{"min":0,"max":100,"step":1}', 'admin', '2024-01-01 00:00:00'),
(15, 4, '电机状态', 'motor_status',   1, 'enum',   NULL, 1, 0, 3, '{"0":"停止","1":"正转","2":"反转"}', 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 物模型数据 -- 烟雾报警器 (product_id=5)
-- -----------------------------------------------------------
INSERT IGNORE INTO `things_model` (`model_id`, `product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`, `specs`, `create_by`, `create_time`)
VALUES
(16, 5, '烟雾浓度', 'smoke_density', 1, 'decimal', 'ppm', 1, 1, 1, '{"min":0,"max":1000,"step":0.1}', 'admin', '2024-01-01 00:00:00'),
(17, 5, '报警状态', 'alarm_status',  1, 'bool',    NULL,  1, 1, 2, '{"true":"报警","false":"正常"}', 'admin', '2024-01-01 00:00:00'),
(18, 5, '电池电量', 'battery',       1, 'integer', '%',   1, 0, 3, '{"min":0,"max":100}', 'admin', '2024-01-01 00:00:00');

-- -----------------------------------------------------------
-- 设备数据 (8 devices in different rooms)
-- -----------------------------------------------------------
INSERT IGNORE INTO `device` (`device_id`, `device_name`, `product_id`, `serial_number`, `device_type`, `status`, `room_id`, `gw_serial_number`, `slave_id`, `last_online_time`, `create_by`, `create_time`)
VALUES
(1, '客厅主灯',       1, 'LIGHT-001', 1, 3, 1, NULL,    NULL, '2024-06-01 08:00:00', 'admin', '2024-01-01 00:00:00'),
(2, '客厅温湿度传感器', 2, 'TH-001',   3, 3, 1, 'GW-001', 1,   '2024-06-01 08:00:00', 'admin', '2024-01-01 00:00:00'),
(3, '主卧智能灯',     1, 'LIGHT-002', 1, 3, 2, NULL,    NULL, '2024-06-01 08:00:00', 'admin', '2024-01-01 00:00:00'),
(4, '客厅智能插座',   3, 'PLUG-001',  1, 3, 1, NULL,    NULL, '2024-06-01 08:00:00', 'admin', '2024-01-15 10:00:00'),
(5, '书房智能窗帘',   4, 'CURTAIN-001',1, 3, 6, NULL,    NULL, '2024-06-01 08:00:00', 'admin', '2024-01-15 10:00:00'),
(6, '厨房烟雾报警器', 5, 'SMOKE-001', 3, 3, 4, 'GW-001', 2,   '2024-06-01 08:00:00', 'admin', '2024-02-01 14:00:00'),
(7, '次卧智能灯',     1, 'LIGHT-003', 1, 4, 3, NULL,    NULL, '2024-05-30 22:00:00', 'admin', '2024-02-01 14:00:00'),
(8, '卫生间温湿度传感器',2,'TH-002',  3, 3, 5, 'GW-001', 3,   '2024-06-01 08:00:00', 'admin', '2024-03-01 09:00:00');

-- -----------------------------------------------------------
-- 场景数据 (6 scenes)
-- -----------------------------------------------------------
INSERT IGNORE INTO `scene` (`scene_id`, `scene_name`, `scene_type`, `enable`, `condition_type`, `execute_mode`, `delay_seconds`, `silent_period`, `icon`, `create_by`, `create_time`)
VALUES
(1, '回家模式', 2, 1, 1, 2, 0, 5,  'icon-home',      'admin', '2024-01-01 00:00:00'),
(2, '离家模式', 2, 1, 1, 2, 0, 5,  'icon-leave',     'admin', '2024-01-01 00:00:00'),
(3, '睡眠模式', 1, 1, 1, 1, 0, 10, 'icon-sleep',     'admin', '2024-01-01 00:00:00'),
(4, '起床模式', 2, 1, 2, 1, 0, 30, 'icon-wakeup',    'admin', '2024-01-01 00:00:00'),
(5, '影院模式', 1, 1, 1, 2, 0, 5,  'icon-movie',     'admin', '2024-02-01 10:00:00'),
(6, '阅读模式', 1, 1, 1, 2, 0, 5,  'icon-reading',   'admin', '2024-02-01 10:00:00');

-- -----------------------------------------------------------
-- 场景触发条件数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `scene_trigger` (`trigger_id`, `scene_id`, `trigger_type`, `device_id`, `model_identifier`, `operator`, `value`, `cron_expression`)
VALUES
-- 回家模式: 客厅温湿度传感器温度 < 18 自动触发
(1, 1, 1, 2, 'temperature', '<', '18', NULL),
-- 离家模式: 手动触发（无设备触发条件）
-- 睡眠模式: 手动触发
-- 起床模式: 定时触发 每天早上7:00
(2, 4, 2, NULL, NULL, NULL, NULL, '0 0 7 * * ?'),
-- 影院模式: 手动触发
-- 阅读模式: 手动触发
-- 离家模式额外: 客厅智能插座功率 > 500 时提醒
(3, 2, 1, 4, 'power', '>', '500', NULL);

-- -----------------------------------------------------------
-- 场景执行动作数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `scene_action` (`action_id`, `scene_id`, `action_type`, `device_id`, `model_identifier`, `value`, `delay_seconds`, `sort_order`)
VALUES
-- 回家模式: 打开客厅主灯(亮度80) + 打开客厅温湿度传感器所在房间窗帘
(1,  1, 1, 1, 'switch',     'true',  0, 1),
(2,  1, 1, 1, 'brightness', '80',    0, 2),
-- 离家模式: 关闭所有灯 + 关闭智能插座
(3,  2, 1, 1, 'switch',     'false', 0, 1),
(4,  2, 1, 3, 'switch',     'false', 0, 2),
(5,  2, 1, 7, 'switch',     'false', 0, 3),
(6,  2, 1, 4, 'switch',     'false', 0, 4),
-- 睡眠模式: 关闭客厅灯 + 关闭书房窗帘 + 主卧灯调暗到10%
(7,  3, 1, 1, 'switch',     'false', 0, 1),
(8,  3, 1, 5, 'curtain_status','0',   0, 2),
(9,  3, 1, 3, 'brightness', '10',    0, 3),
(10, 3, 1, 3, 'switch',     'true',  0, 4),
-- 起床模式: 打开主卧灯(亮度50) + 打开书房窗帘
(11, 4, 1, 3, 'switch',     'true',  0, 1),
(12, 4, 1, 3, 'brightness', '50',    0, 2),
(13, 4, 1, 5, 'curtain_status','1',   5, 3),
-- 影院模式: 关闭客厅主灯亮度调到5
(14, 5, 1, 1, 'switch',     'true',  0, 1),
(15, 5, 1, 1, 'brightness', '5',     0, 2),
-- 阅读模式: 书房灯亮度100 + 色温5000K
(16, 6, 1, 1, 'switch',     'true',  0, 1),
(17, 6, 1, 1, 'brightness', '100',   0, 2),
(18, 6, 1, 1, 'color_temp', '5000',  0, 3);

-- -----------------------------------------------------------
-- 场景执行日志
-- -----------------------------------------------------------
INSERT IGNORE INTO `scene_log` (`log_id`, `scene_id`, `trigger_info`, `action_results`, `status`, `execute_time`)
VALUES
(1, 1, '温度低于18°C自动触发', '[{"actionId":1,"status":"success"},{"actionId":2,"status":"success"}]', 1, '2024-05-20 18:30:00'),
(2, 3, '手动触发', '[{"actionId":7,"status":"success"},{"actionId":8,"status":"success"},{"actionId":9,"status":"success"},{"actionId":10,"status":"success"}]', 1, '2024-05-20 23:00:00'),
(3, 4, '定时触发 07:00', '[{"actionId":11,"status":"success"},{"actionId":12,"status":"success"},{"actionId":13,"status":"success"}]', 1, '2024-05-21 07:00:00'),
(4, 2, '手动触发', '[{"actionId":3,"status":"success"},{"actionId":4,"status":"success"},{"actionId":5,"status":"failed","msg":"device offline"},{"actionId":6,"status":"success"}]', 2, '2024-05-21 08:30:00'),
(5, 5, '手动触发', '[{"actionId":14,"status":"success"},{"actionId":15,"status":"success"}]', 1, '2024-05-21 20:00:00'),
(6, 1, '温度低于18°C自动触发', '[{"actionId":1,"status":"success"},{"actionId":2,"status":"success"}]', 1, '2024-05-22 19:00:00');

-- -----------------------------------------------------------
-- 告警规则数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `alert_rule` (`alert_id`, `alert_name`, `device_id`, `model_identifier`, `operator`, `threshold`, `alert_level`, `notify_type`, `enable`, `create_by`, `create_time`)
VALUES
(1, '高温告警-客厅',     2, 'temperature',   '>',  '35',  3, 'wechat', 1, 'admin', '2024-01-01 00:00:00'),
(2, '低温告警-客厅',     2, 'temperature',   '<',  '5',   2, 'wechat', 1, 'admin', '2024-01-01 00:00:00'),
(3, '高湿度告警-卫生间', 8, 'humidity',      '>',  '85',  2, 'wechat', 1, 'admin', '2024-01-01 00:00:00'),
(4, '烟雾告警-厨房',     6, 'smoke_density', '>',  '200', 3, 'wechat', 1, 'admin', '2024-01-01 00:00:00'),
(5, '过载告警-客厅插座', 4, 'power',         '>',  '2500',3, 'email',  1, 'admin', '2024-02-01 10:00:00'),
(6, '低电量告警-烟雾报警器',6,'battery',     '<',  '20',  2, 'wechat', 1, 'admin', '2024-02-01 10:00:00');

-- -----------------------------------------------------------
-- 告警日志数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `alert_log` (`log_id`, `alert_id`, `device_id`, `device_name`, `alert_value`, `alert_level`, `alert_message`, `status`, `create_time`, `handle_time`, `handle_remark`)
VALUES
(1, 1, 2, '客厅温湿度传感器', '36.5', 3, '客厅温度达到36.5°C，超过阈值35°C', 2, '2024-05-15 14:30:00', '2024-05-15 14:35:00', '已开启空调降温'),
(2, 4, 6, '厨房烟雾报警器',  '258.3', 3, '厨房烟雾浓度258.3ppm，超过阈值200ppm', 2, '2024-05-16 12:15:00', '2024-05-16 12:20:00', '误报，炒菜油烟引起'),
(3, 2, 2, '客厅温湿度传感器', '3.2',  2, '客厅温度降至3.2°C，低于阈值5°C', 1, '2024-05-18 06:00:00', NULL, NULL),
(4, 3, 8, '卫生间温湿度传感器','88.5', 2, '卫生间湿度88.5%，超过阈值85%', 3, '2024-05-19 21:00:00', '2024-05-19 21:30:00', '洗澡后正常现象，已忽略'),
(5, 5, 4, '客厅智能插座',     '2800.0',3, '客厅插座功率2800W，超过阈值2500W', 2, '2024-05-20 10:00:00', '2024-05-20 10:05:00', '已拔除大功率电器'),
(6, 6, 6, '厨房烟雾报警器',   '18',   2, '烟雾报警器电量仅剩18%', 1, '2024-05-21 09:00:00', NULL, NULL),
(7, 1, 2, '客厅温湿度传感器', '37.2', 3, '客厅温度达到37.2°C，超过阈值35°C', 2, '2024-05-22 15:00:00', '2024-05-22 15:10:00', '已开启空调');

-- -----------------------------------------------------------
-- 设备属性历史记录数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `device_property_log` (`log_id`, `device_id`, `identifier`, `value`, `create_time`)
VALUES
-- 客厅温湿度传感器温度历史
(1,  2, 'temperature', '24.5', '2024-05-20 08:00:00'),
(2,  2, 'temperature', '25.1', '2024-05-20 10:00:00'),
(3,  2, 'temperature', '26.8', '2024-05-20 12:00:00'),
(4,  2, 'temperature', '28.3', '2024-05-20 14:00:00'),
(5,  2, 'temperature', '27.2', '2024-05-20 16:00:00'),
(6,  2, 'temperature', '25.6', '2024-05-20 18:00:00'),
(7,  2, 'temperature', '24.1', '2024-05-20 20:00:00'),
(8,  2, 'temperature', '23.5', '2024-05-20 22:00:00'),
-- 客厅温湿度传感器湿度历史
(9,  2, 'humidity',    '55.2', '2024-05-20 08:00:00'),
(10, 2, 'humidity',    '53.8', '2024-05-20 10:00:00'),
(11, 2, 'humidity',    '50.1', '2024-05-20 12:00:00'),
(12, 2, 'humidity',    '48.5', '2024-05-20 14:00:00'),
(13, 2, 'humidity',    '51.3', '2024-05-20 16:00:00'),
(14, 2, 'humidity',    '54.7', '2024-05-20 18:00:00'),
(15, 2, 'humidity',    '56.2', '2024-05-20 20:00:00'),
(16, 2, 'humidity',    '58.1', '2024-05-20 22:00:00'),
-- 客厅主灯开关状态
(17, 1, 'switch',      'true',  '2024-05-20 07:00:00'),
(18, 1, 'switch',      'false', '2024-05-20 08:30:00'),
(19, 1, 'switch',      'true',  '2024-05-20 18:00:00'),
(20, 1, 'switch',      'false', '2024-05-20 23:00:00'),
-- 客厅主灯亮度
(21, 1, 'brightness',  '80',  '2024-05-20 07:00:00'),
(22, 1, 'brightness',  '60',  '2024-05-20 19:00:00'),
(23, 1, 'brightness',  '10',  '2024-05-20 22:00:00'),
-- 客厅智能插座功率
(24, 4, 'power',       '120.5', '2024-05-20 08:00:00'),
(25, 4, 'power',       '250.3', '2024-05-20 10:00:00'),
(26, 4, 'power',       '85.1',  '2024-05-20 12:00:00'),
(27, 4, 'power',       '320.8', '2024-05-20 14:00:00'),
(28, 4, 'power',       '0.0',   '2024-05-20 16:00:00'),
-- 厨房烟雾报警器
(29, 6, 'smoke_density','12.5',  '2024-05-20 08:00:00'),
(30, 6, 'smoke_density','45.2',  '2024-05-20 11:30:00'),
(31, 6, 'smoke_density','128.7', '2024-05-20 12:00:00'),
(32, 6, 'smoke_density','35.1',  '2024-05-20 12:30:00'),
(33, 6, 'smoke_density','8.3',   '2024-05-20 13:00:00'),
-- 卫生间温湿度传感器
(34, 8, 'temperature', '25.8', '2024-05-20 08:00:00'),
(35, 8, 'temperature', '26.3', '2024-05-20 12:00:00'),
(36, 8, 'temperature', '27.1', '2024-05-20 20:00:00'),
(37, 8, 'humidity',    '72.5', '2024-05-20 08:00:00'),
(38, 8, 'humidity',    '68.2', '2024-05-20 12:00:00'),
(39, 8, 'humidity',    '88.5', '2024-05-20 20:00:00');

-- -----------------------------------------------------------
-- 设备分组数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `device_group` (`group_id`, `group_name`, `icon`, `sort_order`, `create_by`, `create_time`)
VALUES
(1, '所有灯光',   'icon-light',   1, 'admin', '2024-01-15 10:00:00'),
(2, '环境监测',   'icon-sensor',  2, 'admin', '2024-01-15 10:00:00'),
(3, '安防设备',   'icon-safety',  3, 'admin', '2024-02-01 10:00:00'),
(4, '窗帘控制',   'icon-curtain', 4, 'admin', '2024-02-01 10:00:00');

-- -----------------------------------------------------------
-- 设备分组-设备关联数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `device_group_device` (`id`, `group_id`, `device_id`)
VALUES
(1, 1, 1),   -- 所有灯光 -> 客厅主灯
(2, 1, 3),   -- 所有灯光 -> 主卧智能灯
(3, 1, 7),   -- 所有灯光 -> 次卧智能灯
(4, 2, 2),   -- 环境监测 -> 客厅温湿度传感器
(5, 2, 8),   -- 环境监测 -> 卫生间温湿度传感器
(6, 3, 6),   -- 安防设备 -> 厨房烟雾报警器
(7, 4, 5);   -- 窗帘控制 -> 书房智能窗帘

-- -----------------------------------------------------------
-- 通知配置数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `notification_config` (`config_id`, `user_id`, `notify_type`, `config`, `enable`, `quiet_start`, `quiet_end`, `create_by`, `create_time`)
VALUES
(1, 1, 'wechat', '{"webhook":"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=demo-key-001"}', 1, '23:00', '07:00', 'admin', '2024-01-01 00:00:00'),
(2, 1, 'email',  '{"to":"admin@smarthome.com","smtp":"smtp.smarthome.com"}',                        1, '23:00', '07:00', 'admin', '2024-01-01 00:00:00'),
(3, 2, 'wechat', '{"webhook":"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=demo-key-002"}', 1, NULL,    NULL,    'admin', '2024-01-15 10:00:00');

-- -----------------------------------------------------------
-- 操作日志数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `operation_log` (`log_id`, `user_id`, `username`, `module`, `operation`, `target`, `detail`, `ip`, `status`, `create_time`)
VALUES
(1,  1, 'admin',   'device', '控制', '客厅主灯',       '打开客厅主灯，亮度设为80%',         '192.168.1.100', 1, '2024-05-20 18:00:00'),
(2,  1, 'admin',   'scene',  '执行', '回家模式',       '手动执行回家模式',                 '192.168.1.100', 1, '2024-05-20 18:00:05'),
(3,  1, 'admin',   'device', '控制', '客厅智能插座',   '关闭客厅智能插座',                 '192.168.1.100', 1, '2024-05-20 20:00:00'),
(4,  2, 'zhangsan','device', '控制', '主卧智能灯',     '打开主卧智能灯，亮度设为50%',       '192.168.1.101', 1, '2024-05-20 21:00:00'),
(5,  1, 'admin',   'scene',  '执行', '睡眠模式',       '手动执行睡眠模式',                 '192.168.1.100', 1, '2024-05-20 23:00:00'),
(6,  1, 'admin',   'alert',  '处理', '高温告警-客厅',   '处理告警日志#1，已开启空调降温',    '192.168.1.100', 1, '2024-05-15 14:35:00'),
(7,  1, 'admin',   'alert',  '处理', '烟雾告警-厨房',   '处理告警日志#2，误报，炒菜油烟引起','192.168.1.100', 1, '2024-05-16 12:20:00'),
(8,  1, 'admin',   'system', '修改', '高温告警阈值',    '将高温告警阈值从30调整为35',       '192.168.1.100', 1, '2024-05-14 10:00:00'),
(9,  1, 'admin',   'device', '创建', '卫生间温湿度传感器','添加新设备：卫生间温湿度传感器',  '192.168.1.100', 1, '2024-03-01 09:00:00'),
(10, 3, 'lisi',    'scene',  '执行', '影院模式',       '手动执行影院模式',                 '192.168.1.102', 1, '2024-05-21 20:00:00');

-- -----------------------------------------------------------
-- 系统配置数据
-- -----------------------------------------------------------
INSERT IGNORE INTO `system_config` (`config_id`, `config_key`, `config_value`, `config_name`, `create_by`, `create_time`)
VALUES
(1, 'site.name',              'SmartHome智能家居',     '站点名称',       'admin', '2024-01-01 00:00:00'),
(2, 'site.version',           '1.0.0',                 '系统版本',       'admin', '2024-01-01 00:00:00'),
(3, 'device.offline.timeout', '300',                   '设备离线超时(秒)','admin', '2024-01-01 00:00:00'),
(4, 'alert.max.level',        '3',                     '最大告警级别',    'admin', '2024-01-01 00:00:00'),
(5, 'data.retention.days',    '90',                    '数据保留天数',    'admin', '2024-01-01 00:00:00'),
(6, 'mqtt.enabled',           'false',                 'MQTT开关',       'admin', '2024-01-01 00:00:00'),
(7, 'notification.enabled',   'true',                  '通知开关',       'admin', '2024-01-01 00:00:00'),
(8, 'scene.max.actions',      '20',                    '场景最大动作数',  'admin', '2024-01-01 00:00:00');
