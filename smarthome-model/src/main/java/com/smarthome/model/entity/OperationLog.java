package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作模块: device/scene/alert/system */
    private String module;

    /** 操作类型: 控制/创建/删除/修改/执行/处理/忽略 */
    private String operation;

    /** 操作目标: 设备名/场景名 */
    private String target;

    /** 具体操作内容 */
    private String detail;

    /** 操作IP */
    private String ip;

    /** 操作状态: 0=失败 1=成功 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
