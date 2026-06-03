package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.utils.JwtUtils;
import com.smarthome.model.dto.LoginDTO;
import com.smarthome.model.dto.RegisterDTO;
import com.smarthome.model.entity.User;
import com.smarthome.model.mapper.UserMapper;
import com.smarthome.model.vo.LoginVO;
import com.smarthome.model.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 处理用户登录、注册、信息查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数（用户名 + 密码）
     * @return LoginVO（token + 用户信息）
     */
    public LoginVO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("该用户已被禁用");
        }

        // BCrypt 密码校验
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成 JWT token
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername());

        // 组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());

        log.info("用户登录成功: {}", user.getUsername());
        return loginVO;
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     */
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户，BCrypt 加密密码
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setSex(2);    // 默认未知
        user.setStatus(1);  // 默认正常

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return UserInfoVO
     */
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
