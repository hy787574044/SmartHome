package com.smarthome.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 VO
 */
@Data
public class OperationLogVO {

    /** 日志ID */
    private Long logId;

    /** 操作用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operation;

    /** 操作目标 */
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
