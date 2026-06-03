package com.smarthome.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭成员
 */
@Data
@TableName("family_member")
public class FamilyMember {

    /** 成员ID */
    @TableId(type = IdType.AUTO)
    private Long memberId;

    /** 家庭ID */
    private Long familyId;

    /** 用户ID */
    private Long userId;

    /** 角色: admin/member/guest */
    private String role;

    /** 加入时间 */
    private LocalDateTime joinTime;
}
