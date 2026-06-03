package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 场景执行动作
 */
@Data
@TableName("scene_action")
public class SceneAction {

    @TableId(type = IdType.AUTO)
    private Long actionId;

    /** 所属场景ID */
    private Long sceneId;

    /** 动作类型: 1=设备控制 2=告警通知 */
    private Integer actionType;

    /** 设备ID（设备控制时） */
    private Long deviceId;

    /** 物模型标识符 */
    private String modelIdentifier;

    /** 动作值 */
    private String value;

    /** 延迟执行（秒） */
    private Integer delaySeconds;

    /** 排序 */
    private Integer sortOrder;
}
