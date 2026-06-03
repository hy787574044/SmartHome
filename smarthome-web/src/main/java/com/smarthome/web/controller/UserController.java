package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.UserService;
import com.smarthome.model.dto.ChangePasswordDTO;
import com.smarthome.model.dto.UpdateAvatarDTO;
import com.smarthome.model.dto.UpdateProfileDTO;
import com.smarthome.model.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户中心 API
 */
@Tag(name = "用户中心")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料
     */
    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public R<UserInfoVO> getProfile() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return R.ok(userService.getProfile(userId));
    }

    /**
     * 更新当前登录用户资料（昵称、邮箱、手机号、性别）
     */
    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateProfile(userId, updateProfileDTO);
        return R.ok(null, "资料更新成功");
    }

    /**
     * 修改密码（需验证旧密码）
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(userId, changePasswordDTO);
        return R.ok(null, "密码修改成功");
    }

    /**
     * 更新头像URL
     */
    @Operation(summary = "更新头像")
    @PutMapping("/avatar")
    public R<Void> updateAvatar(@Valid @RequestBody UpdateAvatarDTO updateAvatarDTO) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateAvatar(userId, updateAvatarDTO);
        return R.ok(null, "头像更新成功");
    }
}
