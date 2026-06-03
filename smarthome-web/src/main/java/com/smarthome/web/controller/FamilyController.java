package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.device.service.FamilyService;
import com.smarthome.model.entity.Family;
import com.smarthome.model.entity.FamilyMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家庭管理 API
 */
@Tag(name = "家庭管理")
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Operation(summary = "创建家庭")
    @PostMapping
    public R<Family> createFamily(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String familyName = body.get("familyName");
        return R.ok(familyService.createFamily(userId, familyName));
    }

    @Operation(summary = "通过邀请码加入家庭")
    @PostMapping("/join")
    public R<FamilyMember> joinFamily(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String inviteCode = body.get("inviteCode");
        return R.ok(familyService.joinFamily(userId, inviteCode));
    }

    @Operation(summary = "获取当前家庭成员")
    @GetMapping("/members")
    public R<List<FamilyMember>> getMembers() {
        Long userId = getCurrentUserId();
        Family family = familyService.getUserFamily(userId);
        if (family == null) {
            return R.ok(java.util.Collections.emptyList());
        }
        return R.ok(familyService.getFamilyMembers(family.getFamilyId()));
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/member/{memberUserId}")
    public R<Void> removeMember(@PathVariable Long memberUserId) {
        Long userId = getCurrentUserId();
        Family family = familyService.getUserFamily(userId);
        if (family == null) {
            return R.fail("您尚未加入任何家庭");
        }
        familyService.removeMember(family.getFamilyId(), memberUserId);
        return R.ok();
    }

    @Operation(summary = "重新生成邀请码")
    @PostMapping("/invite-code")
    public R<Family> regenerateInviteCode() {
        Long userId = getCurrentUserId();
        Family family = familyService.getUserFamily(userId);
        if (family == null) {
            return R.fail("您尚未加入任何家庭");
        }
        return R.ok(familyService.generateInviteCode(family.getFamilyId()));
    }

    @Operation(summary = "获取当前用户所在家庭信息")
    @GetMapping("/info")
    public R<Family> getFamilyInfo() {
        Long userId = getCurrentUserId();
        Family family = familyService.getUserFamily(userId);
        if (family == null) {
            return R.ok(null);
        }
        return R.ok(family);
    }
}
