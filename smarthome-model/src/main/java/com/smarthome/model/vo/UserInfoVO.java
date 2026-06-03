package com.smarthome.model.vo;

import lombok.Data;

/**
 * 用户信息 VO（不含密码，用于接口返回）
 */
@Data
public class UserInfoVO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别: 0=男 1=女 2=未知 */
    private Integer sex;

    /** 状态: 0=禁用 1=正常 */
    private Integer status;
}
