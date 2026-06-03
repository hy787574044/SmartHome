package com.smarthome.model.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新用户资料请求 DTO
 */
@Data
public class UpdateProfileDTO {

    /** 昵称 */
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 性别: 0=男 1=女 2=未知 */
    private Integer sex;
}
