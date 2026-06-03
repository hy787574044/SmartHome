package com.smarthome.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新头像请求 DTO
 */
@Data
public class UpdateAvatarDTO {

    /** 头像URL */
    @NotBlank(message = "头像URL不能为空")
    private String avatar;
}
