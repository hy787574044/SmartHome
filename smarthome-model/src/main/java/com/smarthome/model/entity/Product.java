package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long productId;

    /** 产品名称 */
    private String productName;

    /** 产品分类ID */
    private Long categoryId;

    /** 设备类型: 1=直连设备 2=网关 3=监测设备 */
    private Integer deviceType;

    /** 联网方式: wifi/zigbee/ble/ethernet */
    private String networkMethod;

    /** 产品状态: 0=禁用 1=启用 */
    private Integer status;

    /** 产品描述 */
    private String description;
}
