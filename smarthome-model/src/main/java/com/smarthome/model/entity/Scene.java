package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 场景
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scene")
public class Scene extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long sceneId;

    /** 场景名称 */
    private String sceneName;

    /** 场景类型: 1=手动 2=自动 */
    private Integer sceneType;

    /** 是否启用: 0=禁用 1=启用 */
    private Integer enable;

    /** 触发条件关系: 1=OR(任一满足) 2=AND(全部满足) */
    private Integer conditionType;

    /** 执行模式: 1=串行 2=并行 */
    private Integer executeMode;

    /** 执行延迟（秒） */
    private Integer delaySeconds;

    /** 静默期（分钟，防止重复触发） */
    private Integer silentPeriod;

    /** 场景图标 */
    private String icon;
}
