package com.smarthome.web.controller;

import com.smarthome.common.result.R;
import com.smarthome.model.entity.Scene;
import com.smarthome.model.entity.SceneAction;
import com.smarthome.model.entity.SceneLog;
import com.smarthome.model.entity.SceneTrigger;
import com.smarthome.scene.service.SceneService;
import com.smarthome.common.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 场景联动 API
 */
@Tag(name = "场景联动")
@RestController
@RequestMapping("/api/scene")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;

    @Operation(summary = "创建场景")
    @OperationLog(module = "scene", operation = "创建")
    @PostMapping
    public R<Scene> create(
            @RequestBody SceneCreateRequest request) {
        return R.ok(sceneService.createScene(request.getScene(), request.getTriggers(), request.getActions()));
    }

    @Operation(summary = "更新场景")
    @OperationLog(module = "scene", operation = "修改")
    @PutMapping
    public R<Void> update(@RequestBody Scene scene) {
        sceneService.updateScene(scene);
        return R.ok();
    }

    @Operation(summary = "删除场景")
    @OperationLog(module = "scene", operation = "删除")
    @DeleteMapping("/{sceneId}")
    public R<Void> delete(@PathVariable Long sceneId) {
        sceneService.deleteScene(sceneId);
        return R.ok();
    }

    @Operation(summary = "获取场景详情")
    @GetMapping("/{sceneId}")
    public R<Scene> getById(@PathVariable Long sceneId) {
        return R.ok(sceneService.getScene(sceneId));
    }

    @Operation(summary = "获取场景列表")
    @GetMapping("/list")
    public R<List<Scene>> list() {
        return R.ok(sceneService.listScenes());
    }

    @Operation(summary = "获取场景触发条件")
    @GetMapping("/{sceneId}/triggers")
    public R<List<SceneTrigger>> listTriggers(@PathVariable Long sceneId) {
        return R.ok(sceneService.listTriggers(sceneId));
    }

    @Operation(summary = "获取场景执行动作")
    @GetMapping("/{sceneId}/actions")
    public R<List<SceneAction>> listActions(@PathVariable Long sceneId) {
        return R.ok(sceneService.listActions(sceneId));
    }

    @Operation(summary = "手动执行场景")
    @OperationLog(module = "scene", operation = "执行")
    @PostMapping("/{sceneId}/execute")
    public R<Void> execute(@PathVariable Long sceneId) {
        sceneService.executeScene(sceneId);
        return R.ok();
    }

    @Operation(summary = "获取场景模板列表")
    @GetMapping("/templates")
    public R<List<Map<String, Object>>> listTemplates() {
        return R.ok(sceneService.listSceneTemplates());
    }

    @Operation(summary = "复制场景")
    @OperationLog(module = "scene", operation = "复制")
    @PostMapping("/{sceneId}/copy")
    public R<Scene> copy(@PathVariable Long sceneId) {
        return R.ok(sceneService.copyScene(sceneId));
    }

    @Operation(summary = "获取场景执行日志")
    @GetMapping("/{sceneId}/logs")
    public R<List<SceneLog>> listLogs(@PathVariable Long sceneId) {
        return R.ok(sceneService.listSceneLogs(sceneId));
    }

    /**
     * 创建场景请求体
     */
    @lombok.Data
    public static class SceneCreateRequest {
        private Scene scene;
        private List<SceneTrigger> triggers;
        private List<SceneAction> actions;
    }
}
