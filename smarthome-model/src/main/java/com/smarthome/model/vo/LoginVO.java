package com.smarthome.model.vo;

import lombok.Data;

/**
 * 登录响应 VO（token + 用户信息）
 */
@Data
public class LoginVO {

    /** JWT token */
    private String token;

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
}
