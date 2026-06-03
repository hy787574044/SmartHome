<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <!-- MQTT配置 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><Connection /></el-icon>
          <span>MQTT配置</span>
        </div>
      </template>
      <el-form :model="mqttConfig" label-width="120px" style="max-width: 600px">
        <el-form-item label="服务器地址">
          <el-input v-model="mqttConfig['mqtt.host']" placeholder="MQTT Broker地址" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input v-model="mqttConfig['mqtt.port']" placeholder="1883" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="mqttConfig['mqtt.username']" placeholder="MQTT用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="mqttConfig['mqtt.password']" type="password" show-password placeholder="MQTT密码" />
        </el-form-item>
        <el-form-item label="客户端ID">
          <el-input v-model="mqttConfig['mqtt.client_id']" placeholder="客户端标识" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 告警配置 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><Bell /></el-icon>
          <span>告警配置</span>
        </div>
      </template>
      <el-form :model="alertConfig" label-width="140px" style="max-width: 600px">
        <el-form-item label="默认告警级别">
          <el-select v-model="alertConfig['alert.default_level']" style="width: 100%">
            <el-option label="提示" value="1" />
            <el-option label="警告" value="2" />
            <el-option label="严重" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="静默时间(分钟)">
          <el-input-number v-model.number="alertConfig['alert.silent_minutes']" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item label="每日最大告警数">
          <el-input-number v-model.number="alertConfig['alert.max_per_day']" :min="1" :max="10000" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据清理 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><Delete /></el-icon>
          <span>数据清理</span>
        </div>
      </template>
      <el-form label-width="140px" style="max-width: 600px">
        <el-form-item label="日志保留天数">
          <el-input-number v-model.number="cleanDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="cleaning" @click="handleCleanLogs">
            <el-icon><Delete /></el-icon>
            清理过期日志
          </el-button>
          <span class="tip">将清理{{ cleanDays }}天前的已处理告警日志</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 系统信息 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>系统信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">{{ systemInfo.systemName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本号">{{ systemInfo.version || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运行时间">{{ systemInfo.uptime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="启动时间">{{ formatStartTime(systemInfo.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="设备总数">{{ systemInfo.totalDevices ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="在线设备">{{ systemInfo.onlineDevices ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户总数">{{ systemInfo.totalUsers ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="待处理告警">{{ systemInfo.pendingAlerts ?? '-' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="systemInfo.jvm" style="margin-top: 16px">
        <h4 style="margin-bottom: 12px; color: #94a3b8">JVM信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="最大内存">{{ systemInfo.jvm.maxMemory }}</el-descriptions-item>
          <el-descriptions-item label="已分配内存">{{ systemInfo.jvm.totalMemory }}</el-descriptions-item>
          <el-descriptions-item label="已使用内存">{{ systemInfo.jvm.usedMemory }}</el-descriptions-item>
          <el-descriptions-item label="可用内存">{{ systemInfo.jvm.freeMemory }}</el-descriptions-item>
          <el-descriptions-item label="CPU核数">{{ systemInfo.jvm.availableProcessors }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 保存按钮 -->
    <div class="save-bar">
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">
        <el-icon><Check /></el-icon>
        保存配置
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listConfigs, updateConfigs, getSystemInfo, cleanLogs } from '@/api/system'

const mqttKeys = ['mqtt.host', 'mqtt.port', 'mqtt.username', 'mqtt.password', 'mqtt.client_id']
const alertKeys = ['alert.default_level', 'alert.silent_minutes', 'alert.max_per_day']

const mqttConfig = reactive({})
const alertConfig = reactive({})
const systemInfo = ref({})
const cleanDays = ref(90)
const saving = ref(false)
const cleaning = ref(false)

const loadConfigs = async () => {
  const res = await listConfigs()
  const configs = res.data || []
  configs.forEach(item => {
    if (mqttKeys.includes(item.configKey)) {
      mqttConfig[item.configKey] = item.configValue
    }
    if (alertKeys.includes(item.configKey)) {
      alertConfig[item.configKey] = item.configValue
    }
    if (item.configKey === 'log.retain_days') {
      cleanDays.value = parseInt(item.configValue) || 90
    }
  })
}

const loadSystemInfo = async () => {
  const res = await getSystemInfo()
  systemInfo.value = res.data || {}
}

const handleSave = async () => {
  saving.value = true
  try {
    const data = { ...mqttConfig, ...alertConfig, 'log.retain_days': String(cleanDays.value) }
    await updateConfigs(data)
    ElMessage.success('配置保存成功')
  } finally {
    saving.value = false
  }
}

const handleCleanLogs = async () => {
  await ElMessageBox.confirm(
    `确定要清理${cleanDays.value}天前的已处理告警日志吗？此操作不可恢复。`,
    '清理确认',
    { confirmButtonText: '确定清理', cancelButtonText: '取消', type: 'warning' }
  )
  cleaning.value = true
  try {
    const res = await cleanLogs(cleanDays.value)
    ElMessage.success(res.msg || '清理完成')
    loadSystemInfo()
  } finally {
    cleaning.value = false
  }
}

const formatStartTime = (ts) => {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}

onMounted(() => {
  loadConfigs()
  loadSystemInfo()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: #f0f4f8;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #f0f4f8;

  .el-icon {
    color: #00d4ff;
  }
}

.tip {
  margin-left: 12px;
  font-size: 13px;
  color: #94a3b8;
}

.save-bar {
  text-align: center;
  padding: 20px 0;
}
</style>
