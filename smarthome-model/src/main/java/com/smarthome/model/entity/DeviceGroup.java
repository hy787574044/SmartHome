package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备分组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_group")
public class DeviceGroup extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long groupId;

    /** 分组名称 */
    private String groupName;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;
}
