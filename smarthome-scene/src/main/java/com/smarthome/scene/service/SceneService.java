package com.smarthome.scene.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthome.common.constant.Constants;
import com.smarthome.common.exception.BusinessException;
import com.smarthome.common.utils.RedisUtils;
import com.smarthome.device.service.DeviceService;
import com.smarthome.model.dto.DevicePropertyDTO;
import com.smarthome.model.entity.*;
import com.smarthome.model.mapper.SceneActionMapper;
import com.smarthome.model.mapper.SceneMapper;
import com.smarthome.model.mapper.SceneTriggerMapper;
import com.smarthome.mqtt.service.MqttSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 场景联动服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneMapper sceneMapper;
    private final SceneTriggerMapper triggerMapper;
    private final SceneActionMapper actionMapper;
    private final DeviceService deviceService;
    private final MqttSendService mqttSendService;
    private final RedisUtils redisUtils;

    /**
     * 监听设备属性上报事件 → 检查场景触发
     */
    @EventListener
    public void onDeviceProperty(DeviceService.DevicePropertyEvent event) {
        Device device = event.getDevice();
        List<DevicePropertyDTO> properties = event.getProperties();

        List<Scene> scenes = sceneMapper.selectList(
                new LambdaQueryWrapper<Scene>()
                        .eq(Scene::getSceneType, Constants.SCENE_TYPE_AUTO)
                        .eq(Scene::getEnable, 1)
        );

        for (Scene scene : scenes) {
            try {
                checkAndExecuteScene(scene, device, properties);
            } catch (Exception e) {
                log.error("场景执行异常 - 场景: {}", scene.getSceneName(), e);
            }
        }
    }

    /**
     * 创建场景
     */
    public Scene createScene(Scene scene, List<SceneTrigger> triggers, List<SceneAction> actions) {
        if (triggers == null || triggers.isEmpty()) {
            throw new BusinessException("至少需要一个触发条件");
        }
        if (actions == null || actions.isEmpty()) {
            throw new BusinessException("至少需要一个执行动作");
        }

        sceneMapper.insert(scene);

        for (SceneTrigger trigger : triggers) {
            trigger.setSceneId(scene.getSceneId());
            triggerMapper.insert(trigger);
        }

        for (int i = 0; i < actions.size(); i++) {
            SceneAction action = actions.get(i);
            action.setSceneId(scene.getSceneId());
            action.setSortOrder(i);
            actionMapper.insert(action);
        }

        log.info("场景已创建: {}", scene.getSceneName());
        return scene;
    }

    /**
     * 更新场景
     */
    public void updateScene(Scene scene) {
        sceneMapper.updateById(scene);
    }

    /**
     * 删除场景
     */
    public void deleteScene(Long sceneId) {
        sceneMapper.deleteById(sceneId);
        triggerMapper.delete(new LambdaQueryWrapper<SceneTrigger>().eq(SceneTrigger::getSceneId, sceneId));
        actionMapper.delete(new LambdaQueryWrapper<SceneAction>().eq(SceneAction::getSceneId, sceneId));
    }

    /**
     * 获取场景详情
     */
    public Scene getScene(Long sceneId) {
        return sceneMapper.selectById(sceneId);
    }

    /**
     * 获取场景列表
     */
    public List<Scene> listScenes() {
        return sceneMapper.selectList(new LambdaQueryWrapper<Scene>().orderByDesc(Scene::getCreateTime));
    }

    /**
     * 获取场景的触发条件
     */
    public List<SceneTrigger> listTriggers(Long sceneId) {
        return triggerMapper.selectList(
                new LambdaQueryWrapper<SceneTrigger>().eq(SceneTrigger::getSceneId, sceneId)
        );
    }

    /**
     * 获取场景的执行动作
     */
    public List<SceneAction> listActions(Long sceneId) {
        return actionMapper.selectList(
                new LambdaQueryWrapper<SceneAction>()
                        .eq(SceneAction::getSceneId, sceneId)
                        .orderByAsc(SceneAction::getSortOrder)
        );
    }

    /**
     * 手动执行场景
     */
    public void executeScene(Long sceneId) {
        Scene scene = sceneMapper.selectById(sceneId);
        if (scene == null) {
            throw new BusinessException("场景不存在");
        }
        if (scene.getEnable() != 1) {
            throw new BusinessException("场景未启用");
        }
        executeActions(scene);
    }

    /**
     * 检查场景条件并执行
     */
    private void checkAndExecuteScene(Scene scene, Device device, List<DevicePropertyDTO> properties) {
        String silentKey = Constants.REDIS_SCENE_SILENT + scene.getSceneId();
        if (Boolean.TRUE.equals(redisUtils.hasKey(silentKey))) {
            return;
        }

        List<SceneTrigger> triggers = listTriggers(scene.getSceneId());
        if (triggers.isEmpty()) {
            return;
        }

        boolean triggered;
        if (scene.getConditionType() == 2) {
            triggered = triggers.stream().allMatch(t -> checkTrigger(t, device, properties));
        } else {
            triggered = triggers.stream().anyMatch(t -> checkTrigger(t, device, properties));
        }

        if (triggered) {
            log.info("场景触发 - 场景: {}, 设备: {}", scene.getSceneName(), device.getDeviceName());

            if (scene.getSilentPeriod() != null && scene.getSilentPeriod() > 0) {
                redisUtils.set(silentKey, "1", scene.getSilentPeriod(), TimeUnit.MINUTES);
            }

            executeActions(scene);
        }
    }

    /**
     * 检查单个触发条件
     */
    private boolean checkTrigger(SceneTrigger trigger, Device device, List<DevicePropertyDTO> properties) {
        if (trigger.getTriggerType() == Constants.TRIGGER_TYPE_TIMER) {
            return false; // 定时触发由定时任务处理
        }

        if (trigger.getTriggerType() == Constants.TRIGGER_TYPE_CONDITION) {
            // 条件触发：检查设备属性
            if (!device.getDeviceId().equals(trigger.getDeviceId())) {
                return false;
            }
            for (DevicePropertyDTO prop : properties) {
                if (prop.getId().equals(trigger.getModelIdentifier())) {
                    return compareValue(prop.getValue(), trigger.getOperator(), trigger.getValue());
                }
            }
        }

        return false;
    }

    /**
     * 比较值
     */
    private boolean compareValue(String actual, String operator, String expected) {
        if (actual == null || operator == null || expected == null) {
            return false;
        }
        try {
            double actualNum = Double.parseDouble(actual);
            double expectedNum = Double.parseDouble(expected);
            switch (operator) {
                case "=": return actualNum == expectedNum;
                case "!=": return actualNum != expectedNum;
                case ">": return actualNum > expectedNum;
                case "<": return actualNum < expectedNum;
                case ">=": return actualNum >= expectedNum;
                case "<=": return actualNum <= expectedNum;
                default: return actual.equals(expected);
            }
        } catch (NumberFormatException e) {
            return actual.equals(expected);
        }
    }

    /**
     * 执行场景动作
     */
    private void executeActions(Scene scene) {
        List<SceneAction> actions = listActions(scene.getSceneId());
        for (SceneAction action : actions) {
            try {
                if (action.getDelaySeconds() != null && action.getDelaySeconds() > 0) {
                    Thread.sleep(action.getDelaySeconds() * 1000L);
                }

                if (action.getActionType() == Constants.ACTION_TYPE_DEVICE) {
                    deviceService.controlDevice(action.getDeviceId(), action.getModelIdentifier(), action.getValue());
                    log.info("场景动作执行 - 设备控制: deviceId={}, {}={}",
                            action.getDeviceId(), action.getModelIdentifier(), action.getValue());
                } else if (action.getActionType() == Constants.ACTION_TYPE_ALERT) {
                    log.info("场景动作执行 - 告警通知: sceneId={}", scene.getSceneId());
                }
            } catch (Exception e) {
                log.error("场景动作执行异常", e);
            }
        }
    }

    /**
     * 定时任务：每分钟检查定时触发的场景
     * 使用 Spring CronExpression 进行精确匹配
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkTimerTriggers() {
        List<SceneTrigger> timerTriggers = triggerMapper.selectList(
                new LambdaQueryWrapper<SceneTrigger>()
                        .eq(SceneTrigger::getTriggerType, Constants.TRIGGER_TYPE_TIMER)
                        .isNotNull(SceneTrigger::getCronExpression)
        );

        LocalDateTime now = LocalDateTime.now();
        for (SceneTrigger trigger : timerTriggers) {
            try {
                Scene scene = sceneMapper.selectById(trigger.getSceneId());
                if (scene == null || scene.getEnable() != 1) {
                    continue;
                }

                // 使用 Spring 的 CronExpression 进行匹配
                org.springframework.scheduling.support.CronExpression cron =
                        org.springframework.scheduling.support.CronExpression.parse(trigger.getCronExpression());

                // 检查当前时间是否匹配（比较下一次执行时间是否在当前分钟内）
                LocalDateTime nextMatch = cron.next(now.minusMinutes(1));
                if (nextMatch != null && nextMatch.getMinute() == now.getMinute()
                        && nextMatch.getHour() == now.getHour()
                        && nextMatch.getDayOfMonth() == now.getDayOfMonth()) {
                    log.info("定时触发 - 场景: {}", scene.getSceneName());
                    executeActions(scene);
                }
            } catch (Exception e) {
                log.error("定时触发检查异常 - triggerId: {}", trigger.getTriggerId(), e);
            }
        }
    }
}
