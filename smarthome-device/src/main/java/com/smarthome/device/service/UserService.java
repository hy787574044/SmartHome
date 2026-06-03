package com.smarthome.device.service;

import com.smarthome.common.exception.BusinessException;
import com.smarthome.model.dto.ChangePasswordDTO;
import com.smarthome.model.dto.UpdateAvatarDTO;
import com.smarthome.model.dto.UpdateProfileDTO;
import com.smarthome.model.entity.User;
import com.smarthome.model.mapper.UserMapper;
import com.smarthome.model.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户资料服务
 * 处理个人信息查询、资料修改、密码变更、头像更新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户资料
     *
     * @param userId 用户ID
     * @return UserInfoVO
     */
    public UserInfoVO getProfile(Long userId) {
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

    /**
     * 更新用户资料（昵称、邮箱、手机号、性别）
     *
     * @param userId          用户ID
     * @param updateProfileDTO 更新内容
     */
    public void updateProfile(Long userId, UpdateProfileDTO updateProfileDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (updateProfileDTO.getNickname() != null) {
            user.setNickname(updateProfileDTO.getNickname());
        }
        if (updateProfileDTO.getEmail() != null) {
            user.setEmail(updateProfileDTO.getEmail());
        }
        if (updateProfileDTO.getPhone() != null) {
            user.setPhone(updateProfileDTO.getPhone());
        }
        if (updateProfileDTO.getSex() != null) {
            user.setSex(updateProfileDTO.getSex());
        }

        userMapper.updateById(user);
        log.info("用户资料更新成功: userId={}", userId);
    }

    /**
     * 修改密码（需验证旧密码）
     *
     * @param userId          用户ID
     * @param changePasswordDTO 旧密码 + 新密码
     */
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 新密码不能与旧密码相同
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户密码修改成功: userId={}", userId);
    }

    /**
     * 更新头像URL
     *
     * @param userId         用户ID
     * @param updateAvatarDTO 新头像URL
     */
    public void updateAvatar(Long userId, UpdateAvatarDTO updateAvatarDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setAvatar(updateAvatarDTO.getAvatar());
        userMapper.updateById(user);
        log.info("用户头像更新成功: userId={}", userId);
    }
}
