package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 场景执行日志
 */
@Data
@TableName("scene_log")
public class SceneLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 场景ID */
    private Long sceneId;

    /** 触发信息 */
    private String triggerInfo;

    /** 执行结果(JSON) */
    private String actionResults;

    /** 状态: 0=失败 1=成功 2=部分成功 */
    private Integer status;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;
}
