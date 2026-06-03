-- =============================================
-- SmartHome 全屋智能控制系统 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS smarthome DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smarthome;

-- -------------------------------------------
-- 产品模板表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
    `product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `device_type` TINYINT DEFAULT 1 COMMENT '设备类型: 1=直连 2=网关 3=监测',
    `network_method` VARCHAR(20) DEFAULT 'wifi' COMMENT '联网方式: wifi/zigbee/ble/ethernet',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`product_id`)
) ENGINE=InnoDB COMMENT='产品模板';

-- -------------------------------------------
-- 物模型定义表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `things_model` (
    `model_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `identifier` VARCHAR(100) NOT NULL COMMENT '标识符',
    `type` TINYINT NOT NULL COMMENT '类型: 1=属性 2=功能 3=事件',
    `data_type` VARCHAR(20) DEFAULT 'string' COMMENT '数据类型: integer/decimal/string/bool/enum',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `readonly` TINYINT DEFAULT 0 COMMENT '是否只读: 0=否 1=是',
    `show_index` TINYINT DEFAULT 0 COMMENT '首页展示: 0=否 1=是',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `specs` JSON DEFAULT NULL COMMENT '规格(JSON)',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`model_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB COMMENT='物模型定义';

-- -------------------------------------------
-- 房间表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `room` (
    `room_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `room_name` VARCHAR(50) NOT NULL COMMENT '房间名称',
    `room_type` VARCHAR(20) DEFAULT NULL COMMENT '房间类型: living_room/bedroom/kitchen/bathroom/study/balcony',
    `floor` INT DEFAULT 1 COMMENT '楼层',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`room_id`)
) ENGINE=InnoDB COMMENT='房间';

-- -------------------------------------------
-- 设备表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `device` (
    `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `serial_number` VARCHAR(100) NOT NULL COMMENT '设备序列号',
    `device_type` TINYINT DEFAULT 1 COMMENT '设备类型: 1=直连 2=网关 3=监测',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1=未激活 2=禁用 3=在线 4=离线',
    `room_id` BIGINT DEFAULT NULL COMMENT '房间ID',
    `gw_serial_number` VARCHAR(100) DEFAULT NULL COMMENT '网关序列号',
    `slave_id` INT DEFAULT NULL COMMENT '子设备地址',
    `last_online_time` DATETIME DEFAULT NULL COMMENT '最后上线时间',
    `last_offline_time` DATETIME DEFAULT NULL COMMENT '最后离线时间',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_serial_number` (`serial_number`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='设备';

-- -------------------------------------------
-- 场景表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `scene` (
    `scene_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '场景ID',
    `scene_name` VARCHAR(100) NOT NULL COMMENT '场景名称',
    `scene_type` TINYINT DEFAULT 1 COMMENT '场景类型: 1=手动 2=自动',
    `enable` TINYINT DEFAULT 1 COMMENT '是否启用: 0=禁用 1=启用',
    `condition_type` TINYINT DEFAULT 1 COMMENT '条件关系: 1=OR 2=AND',
    `execute_mode` TINYINT DEFAULT 1 COMMENT '执行模式: 1=串行 2=并行',
    `delay_seconds` INT DEFAULT 0 COMMENT '执行延迟(秒)',
    `silent_period` INT DEFAULT 0 COMMENT '静默期(分钟)',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`scene_id`)
) ENGINE=InnoDB COMMENT='场景';

-- -------------------------------------------
-- 场景触发条件表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `scene_trigger` (
    `trigger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '触发ID',
    `scene_id` BIGINT NOT NULL COMMENT '场景ID',
    `trigger_type` TINYINT NOT NULL COMMENT '触发类型: 1=设备 2=定时 3=条件',
    `device_id` BIGINT DEFAULT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(100) DEFAULT NULL COMMENT '物模型标识符',
    `operator` VARCHAR(10) DEFAULT NULL COMMENT '运算符: =, !=, >, <, >=, <=',
    `value` VARCHAR(200) DEFAULT NULL COMMENT '触发值',
    `cron_expression` VARCHAR(50) DEFAULT NULL COMMENT 'cron表达式',
    PRIMARY KEY (`trigger_id`),
    KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB COMMENT='场景触发条件';

-- -------------------------------------------
-- 场景执行动作表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `scene_action` (
    `action_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '动作ID',
    `scene_id` BIGINT NOT NULL COMMENT '场景ID',
    `action_type` TINYINT NOT NULL COMMENT '动作类型: 1=设备控制 2=告警通知',
    `device_id` BIGINT DEFAULT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(100) DEFAULT NULL COMMENT '物模型标识符',
    `value` VARCHAR(200) DEFAULT NULL COMMENT '动作值',
    `delay_seconds` INT DEFAULT 0 COMMENT '延迟执行(秒)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`action_id`),
    KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB COMMENT='场景执行动作';

-- -------------------------------------------
-- 告警规则表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `alert_rule` (
    `alert_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `alert_name` VARCHAR(100) NOT NULL COMMENT '告警名称',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `model_identifier` VARCHAR(100) NOT NULL COMMENT '物模型标识符',
    `operator` VARCHAR(10) NOT NULL COMMENT '运算符',
    `threshold` VARCHAR(200) NOT NULL COMMENT '阈值',
    `alert_level` TINYINT DEFAULT 1 COMMENT '告警级别: 1=提示 2=警告 3=严重',
    `notify_type` VARCHAR(50) DEFAULT 'log' COMMENT '通知方式',
    `enable` TINYINT DEFAULT 1 COMMENT '是否启用',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`alert_id`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB COMMENT='告警规则';

-- -------------------------------------------
-- 告警日志表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `alert_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `alert_id` BIGINT DEFAULT NULL COMMENT '告警规则ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    `alert_value` VARCHAR(200) DEFAULT NULL COMMENT '告警值',
    `alert_level` TINYINT DEFAULT 1 COMMENT '告警级别',
    `alert_message` VARCHAR(500) DEFAULT NULL COMMENT '告警消息',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1=未处理 2=已处理 3=已忽略',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    PRIMARY KEY (`log_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='告警日志';

-- -------------------------------------------
-- 设备属性历史记录表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `device_property_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `identifier` VARCHAR(100) NOT NULL COMMENT '物模型标识符',
    `value` VARCHAR(200) DEFAULT NULL COMMENT '属性值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='设备属性历史记录';

-- =============================================
-- 初始化示例数据
-- =============================================

-- 示例房间
INSERT INTO `room` (`room_name`, `room_type`, `floor`, `sort_order`) VALUES
('客厅', 'living_room', 1, 1),
('主卧', 'bedroom', 1, 2),
('次卧', 'bedroom', 1, 3),
('厨房', 'kitchen', 1, 4),
('卫生间', 'bathroom', 1, 5),
('书房', 'study', 1, 6);

-- 示例产品：智能灯
INSERT INTO `product` (`product_name`, `device_type`, `network_method`, `description`) VALUES
('智能灯', 1, 'wifi', 'WiFi 智能灯泡，支持亮度和色温调节'),
('温湿度传感器', 3, 'wifi', 'WiFi 温湿度传感器'),
('智能插座', 1, 'wifi', 'WiFi 智能插座，支持电量统计'),
('智能窗帘', 1, 'wifi', 'WiFi 智能窗帘电机'),
('烟雾报警器', 3, 'wifi', 'WiFi 烟雾报警传感器');

-- 智能灯物模型
INSERT INTO `things_model` (`product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`) VALUES
(1, '开关', 'switch', 2, 'bool', NULL, 0, 1, 1),
(1, '亮度', 'brightness', 1, 'integer', '%', 0, 1, 2),
(1, '色温', 'color_temp', 1, 'integer', 'K', 0, 0, 3);

-- 温湿度传感器物模型
INSERT INTO `things_model` (`product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`) VALUES
(2, '温度', 'temperature', 1, 'decimal', '℃', 1, 1, 1),
(2, '湿度', 'humidity', 1, 'decimal', '%RH', 1, 1, 2);

-- 智能插座物模型
INSERT INTO `things_model` (`product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`) VALUES
(3, '开关', 'switch', 2, 'bool', NULL, 0, 1, 1),
(3, '功率', 'power', 1, 'decimal', 'W', 1, 1, 2),
(3, '电量', 'electricity', 1, 'decimal', 'kWh', 1, 0, 3);

-- 智能窗帘物模型
INSERT INTO `things_model` (`product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`) VALUES
(4, '开关', 'switch', 2, 'bool', NULL, 0, 1, 1),
(4, '开合度', 'position', 1, 'integer', '%', 0, 1, 2);

-- 烟雾报警器物模型
INSERT INTO `things_model` (`product_id`, `model_name`, `identifier`, `type`, `data_type`, `unit`, `readonly`, `show_index`, `sort_order`) VALUES
(5, '烟雾浓度', 'smoke_level', 1, 'integer', 'ppm', 1, 1, 1),
(5, '报警状态', 'alarm', 3, 'bool', NULL, 1, 1, 2);

-- -------------------------------------------
-- 系统用户表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `sex` TINYINT DEFAULT 0 COMMENT '性别: 0=未知 1=男 2=女',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='系统用户';

-- 默认管理员账户 (密码: admin123)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', 1);
