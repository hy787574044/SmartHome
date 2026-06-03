package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.AuthService;
import com.smarthome.model.dto.LoginDTO;
import com.smarthome.model.dto.RegisterDTO;
import com.smarthome.model.vo.LoginVO;
import com.smarthome.model.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证接口
 */
@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 验证用户名密码，成功返回 token + 用户信息
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return R.ok(loginVO, "登录成功");
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return R.ok(null, "注册成功");
    }

    /**
     * 获取当前用户信息
     * 从 JWT token 中解析 userId
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo() {
        // JwtAuthenticationFilter 将 userId 存入 SecurityContext 的 principal
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoVO userInfo = authService.getUserInfo(userId);
        return R.ok(userInfo);
    }
}
