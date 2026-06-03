<template>
  <div class="templates-page">
    <div class="page-header">
      <h2>场景模板</h2>
      <el-button text class="back-btn" @click="$router.push('/scene')">
        <el-icon><ArrowLeft /></el-icon> 返回场景列表
      </el-button>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="templates.length === 0" class="empty-state">
      <el-empty description="暂无场景模板" :image-size="100" />
    </div>

    <div v-else class="templates-grid">
      <div
        v-for="template in templates"
        :key="template.templateId"
        class="template-card"
      >
        <div class="template-card__glow"></div>
        <div class="template-card__icon">
          <el-icon :size="32">
            <component :is="getTemplateIcon(template.icon)" />
          </el-icon>
        </div>
        <div class="template-card__body">
          <h3 class="template-card__name">{{ template.templateName }}</h3>
          <p class="template-card__desc">{{ template.description }}</p>
          <div class="template-card__actions">
            <div class="action-label">包含动作：</div>
            <ul class="action-list">
              <li v-for="(action, idx) in template.actionList" :key="idx" class="action-item">
                <el-icon :size="12"><CircleCheck /></el-icon>
                {{ action }}
              </li>
            </ul>
          </div>
        </div>
        <div class="template-card__footer">
          <el-button
            type="primary"
            class="use-btn"
            @click="handleUseTemplate(template)"
          >
            <el-icon><Plus /></el-icon> 使用此模板
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSceneTemplates } from '@/api/scene'
import {
  Sunny,
  Moon,
  HomeFilled,
  SwitchButton,
  Clock,
  Cloudy,
  Lightning,
  Setting,
  CircleCheck,
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const templates = ref([])

const iconMap = {
  sunny: Sunny,
  moon: Moon,
  home: HomeFilled,
  leave: SwitchButton,
  sleep: Moon,
  wakeup: Sunny,
  cinema: Lightning,
  read: Setting,
}

const getTemplateIcon = (iconName) => {
  return iconMap[iconName] || Setting
}

const loadTemplates = async () => {
  try {
    const res = await getSceneTemplates()
    templates.value = res.data || []
  } catch (e) {
    console.error('加载场景模板失败:', e)
    // Use fallback demo data if API not available
    templates.value = [
      {
        templateId: 1,
        templateName: '回家模式',
        icon: 'home',
        description: '自动打开客厅灯光，调节空调至舒适温度，开启窗帘',
        actionList: ['打开客厅主灯', '空调设为26度', '打开窗帘'],
        triggers: [{ triggerType: 1, modelIdentifier: 'door_sensor', operator: '=', value: 'open' }],
        actions: [
          { actionType: 1, modelIdentifier: 'light_switch', value: 'on' },
          { actionType: 1, modelIdentifier: 'ac_mode', value: 'cool_26' },
          { actionType: 1, modelIdentifier: 'curtain_switch', value: 'open' },
        ],
      },
      {
        templateId: 2,
        templateName: '离家模式',
        icon: 'away',
        description: '关闭所有灯光和电器，开启安防监控',
        actionList: ['关闭所有灯光', '关闭空调', '开启安防模式'],
        triggers: [{ triggerType: 3, modelIdentifier: 'presence_sensor', operator: '=', value: 'away' }],
        actions: [
          { actionType: 1, modelIdentifier: 'light_switch', value: 'off' },
          { actionType: 1, modelIdentifier: 'ac_power', value: 'off' },
          { actionType: 1, modelIdentifier: 'security_mode', value: 'armed' },
        ],
      },
      {
        templateId: 3,
        templateName: '睡眠模式',
        icon: 'moon',
        description: '关闭卧室以外灯光，调节空调为睡眠模式',
        actionList: ['关闭客厅灯光', '卧室灯光调至最暗', '空调切换睡眠模式'],
        triggers: [{ triggerType: 2, cronExpression: '0 0 23 * * ?' }],
        actions: [
          { actionType: 1, modelIdentifier: 'living_room_light', value: 'off' },
          { actionType: 1, modelIdentifier: 'bedroom_light', value: '10' },
          { actionType: 1, modelIdentifier: 'ac_mode', value: 'sleep' },
        ],
      },
      {
        templateId: 4,
        templateName: '起床模式',
        icon: 'sunny',
        description: '早晨自动打开窗帘，调节灯光亮度',
        actionList: ['打开卧室窗帘', '灯光调至50%', '播放轻音乐'],
        triggers: [{ triggerType: 2, cronExpression: '0 0 7 * * ?' }],
        actions: [
          { actionType: 1, modelIdentifier: 'bedroom_curtain', value: 'open' },
          { actionType: 1, modelIdentifier: 'bedroom_light', value: '50' },
        ],
      },
      {
        templateId: 5,
        templateName: '定时巡检',
        icon: 'clock',
        description: '定时检查所有设备状态，异常时发送告警',
        actionList: ['检查设备在线状态', '检测传感器数值', '异常发送通知'],
        triggers: [{ triggerType: 2, cronExpression: '0 0 */6 * * ?' }],
        actions: [
          { actionType: 2, modelIdentifier: 'device_check', value: 'all' },
          { actionType: 2, modelIdentifier: 'alert_notify', value: 'abnormal' },
        ],
      },
      {
        templateId: 6,
        templateName: '节能模式',
        icon: 'lightning',
        description: '自动优化用电设备功耗，降低能耗',
        actionList: ['空调调至节能温度', '关闭非必要照明', '进入低功耗模式'],
        triggers: [{ triggerType: 3, modelIdentifier: 'power_usage', operator: '>', value: '500' }],
        actions: [
          { actionType: 1, modelIdentifier: 'ac_mode', value: 'eco' },
          { actionType: 1, modelIdentifier: 'light_brightness', value: '30' },
        ],
      },
    ]
  } finally {
    loading.value = false
  }
}

const handleUseTemplate = (template) => {
  // Navigate to scene list page with template data for prefilled creation
  router.push({
    path: '/scene',
    query: { fromTemplate: '1' },
    state: { templateData: template },
  })
}

onMounted(() => {
  loadTemplates()
})
</script>

<style lang="scss" scoped>
.templates-page {
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #f0f4f8;
  }

  .back-btn {
    color: #00d4ff;
    font-size: 14px;
    &:hover { color: #33ddff; }
  }
}

.loading-wrap {
  padding: 24px;
}

.empty-state {
  padding: 60px 0;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.template-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: rgba(15, 21, 53, 0.85);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 14px;
  padding: 24px;
  backdrop-filter: blur(12px);
  overflow: hidden;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(0, 212, 255, 0.3);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }

  &__glow {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, #00d4ff, #7b61ff, transparent);
    border-radius: 14px 14px 0 0;
  }

  &__icon {
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
    background: rgba(0, 212, 255, 0.1);
    color: #00d4ff;
    margin-bottom: 16px;
    flex-shrink: 0;
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__name {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 600;
    color: #f0f4f8;
  }

  &__desc {
    margin: 0 0 16px 0;
    font-size: 13px;
    color: #94a3b8;
    line-height: 1.6;
  }

  &__actions {
    margin-bottom: 16px;
  }

  &__footer {
    margin-top: auto;
  }
}

.action-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.action-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #b0bcc8;

  .el-icon {
    color: #22c55e;
  }
}

.use-btn {
  width: 100%;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(123, 97, 255, 0.15)) !important;
  border: 1px solid rgba(0, 212, 255, 0.3) !important;
  color: #f0f4f8 !important;
  font-weight: 500;
  transition: all 0.3s;

  &:hover {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.35), rgba(123, 97, 255, 0.25)) !important;
    border-color: rgba(0, 212, 255, 0.5) !important;
    box-shadow: 0 4px 16px rgba(0, 212, 255, 0.2);
  }
}

@media (max-width: 1200px) {
  .templates-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .templates-grid {
    grid-template-columns: 1fr;
  }
}
</style>
