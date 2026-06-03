package com.smarthome.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordDTO {

    /** 旧密码 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6~20 个字符")
    private String newPassword;
}
