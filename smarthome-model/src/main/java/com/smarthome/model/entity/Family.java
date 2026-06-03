package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthome.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("family")
public class Family extends BaseEntity {

    /** 家庭ID */
    @TableId(type = IdType.AUTO)
    private Long familyId;

    /** 家庭名称 */
    private String familyName;

    /** 创建者ID */
    private Long creatorId;

    /** 邀请码（6位随机） */
    private String inviteCode;

    /** 状态: 0=禁用 1=正常 */
    private Integer status;
}
