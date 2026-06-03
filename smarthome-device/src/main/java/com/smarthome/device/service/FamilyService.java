package com.smarthome.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.model.entity.Family;
import com.smarthome.model.entity.FamilyMember;
import com.smarthome.model.mapper.FamilyMapper;
import com.smarthome.model.mapper.FamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 家庭管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;

    /**
     * 创建家庭，创建者自动成为 admin
     *
     * @param userId     创建者用户ID
     * @param familyName 家庭名称
     * @return 创建的家庭
     */
    @Transactional(rollbackFor = Exception.class)
    public Family createFamily(Long userId, String familyName) {
        // 创建家庭
        Family family = new Family();
        family.setFamilyName(familyName);
        family.setCreatorId(userId);
        family.setInviteCode(generateRandomCode());
        family.setStatus(1);
        familyMapper.insert(family);

        // 创建者自动成为 admin
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getFamilyId());
        member.setUserId(userId);
        member.setRole("admin");
        member.setJoinTime(LocalDateTime.now());
        familyMemberMapper.insert(member);

        log.info("创建家庭成功: familyId={}, userId={}", family.getFamilyId(), userId);
        return family;
    }

    /**
     * 通过邀请码加入家庭
     *
     * @param userId     用户ID
     * @param inviteCode 邀请码
     * @return 家庭成员
     */
    @Transactional(rollbackFor = Exception.class)
    public FamilyMember joinFamily(Long userId, String inviteCode) {
        // 查询家庭
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getInviteCode, inviteCode)
        );
        if (family == null) {
            throw new BusinessException("邀请码无效");
        }
        if (family.getStatus() == 0) {
            throw new BusinessException("该家庭已被禁用");
        }

        // 检查是否已是成员
        Long count = familyMemberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, family.getFamilyId())
                        .eq(FamilyMember::getUserId, userId)
        );
        if (count > 0) {
            throw new BusinessException("您已是该家庭的成员");
        }

        // 添加成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getFamilyId());
        member.setUserId(userId);
        member.setRole("member");
        member.setJoinTime(LocalDateTime.now());
        familyMemberMapper.insert(member);

        log.info("用户加入家庭成功: userId={}, familyId={}", userId, family.getFamilyId());
        return member;
    }

    /**
     * 移除家庭成员
     *
     * @param familyId 家庭ID
     * @param userId   要移除的用户ID
     */
    public void removeMember(Long familyId, Long userId) {
        int deleted = familyMemberMapper.delete(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
                        .eq(FamilyMember::getUserId, userId)
        );
        if (deleted == 0) {
            throw new BusinessException("成员不存在");
        }
        log.info("移除家庭成员成功: familyId={}, userId={}", familyId, userId);
    }

    /**
     * 获取家庭成员列表
     *
     * @param familyId 家庭ID
     * @return 成员列表
     */
    public List<FamilyMember> getFamilyMembers(Long familyId) {
        return familyMemberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
                        .orderByAsc(FamilyMember::getJoinTime)
        );
    }

    /**
     * 重新生成邀请码
     *
     * @param familyId 家庭ID
     * @return 更新后的家庭
     */
    public Family generateInviteCode(Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }
        family.setInviteCode(generateRandomCode());
        familyMapper.updateById(family);
        log.info("重新生成邀请码: familyId={}, newCode={}", familyId, family.getInviteCode());
        return family;
    }

    /**
     * 获取用户所在的家庭信息（取第一个）
     *
     * @param userId 用户ID
     * @return 家庭信息，不存在返回 null
     */
    public Family getUserFamily(Long userId) {
        // 先查成员表获取家庭ID
        FamilyMember member = familyMemberMapper.selectOne(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (member == null) {
            return null;
        }
        return familyMapper.selectById(member.getFamilyId());
    }

    /**
     * 生成6位随机邀请码（数字+大写字母）
     */
    private String generateRandomCode() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
