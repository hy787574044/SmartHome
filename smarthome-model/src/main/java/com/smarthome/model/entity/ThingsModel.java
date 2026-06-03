package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物模型定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("things_model")
public class ThingsModel extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long modelId;

    /** 所属产品ID */
    private Long productId;

    /** 模型名称（如：温度、开关） */
    private String modelName;

    /** 标识符（如：temperature、switch） */
    private String identifier;

    /** 类型: 1=属性 2=功能 3=事件 */
    private Integer type;

    /** 数据类型: integer/decimal/string/bool/enum */
    private String dataType;

    /** 单位 */
    private String unit;

    /** 是否只读: 0=否 1=是 */
    private Integer readonly;

    /** 是否在首页展示: 0=否 1=是 */
    private Integer showIndex;

    /** 排序 */
    private Integer sortOrder;

    /** 规格（JSON：枚举值、取值范围等） */
    private String specs;
}
