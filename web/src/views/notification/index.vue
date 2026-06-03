<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>通知设置</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加通道
      </el-button>
    </div>

    <!-- 通知通道列表 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="card-header">
          <el-icon><Message /></el-icon>
          <span>通知通道</span>
        </div>
      </template>

      <div v-if="configs.length === 0" class="empty-state">
        <el-empty description="暂无通知通道，请添加" :image-size="80" />
      </div>

      <div v-else class="config-list">
        <div v-for="config in configs" :key="config.configId" class="config-item">
          <div class="config-icon" :class="`config-icon--${config.channelType}`">
            <el-icon :size="24">
              <ChatDotRound v-if="config.channelType === 'wechat'" />
              <Message v-else-if="config.channelType === 'email'" />
              <Iphone v-else />
            </el-icon>
          </div>
          <div class="config-body">
            <div class="config-name">{{ config.configName }}</div>
            <div class="config-meta">
              <el-tag :type="channelTagType(config.channelType)" size="small">
                {{ channelTypeMap[config.channelType] }}
              </el-tag>
              <span class="config-target">{{ config.target }}</span>
              <el-tag v-if="config.status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="info" size="small">禁用</el-tag>
            </div>
          </div>
          <div class="config-actions">
            <el-button text type="primary" size="small" @click="handleTest(config)">
              <el-icon><Promotion /></el-icon> 测试
            </el-button>
            <el-button text type="primary" size="small" @click="showEditDialog(config)">编辑</el-button>
            <el-popconfirm title="确定删除该通知通道？" @confirm="handleDelete(config.configId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 免打扰设置 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>免打扰时段</span>
        </div>
      </template>
      <div class="quiet-section">
        <div class="quiet-toggle">
          <span class="quiet-label">启用免打扰</span>
          <el-switch v-model="quietEnabled" @change="handleQuietToggle" />
        </div>
        <div v-if="quietEnabled" class="quiet-time">
          <el-time-picker
            v-model="quietStart"
            placeholder="开始时间"
            format="HH:mm"
            style="width: 140px"
          />
          <span class="quiet-sep">至</span>
          <el-time-picker
            v-model="quietEnd"
            placeholder="结束时间"
            format="HH:mm"
            style="width: 140px"
          />
          <el-button type="primary" size="small" style="margin-left: 12px" @click="saveQuietHours">保存</el-button>
        </div>
        <p class="quiet-tip">免打扰时段内，系统将暂停发送通知消息</p>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑通知通道' : '添加通知通道'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="通道名称" required>
          <el-input v-model="form.configName" placeholder="如：微信告警通知" />
        </el-form-item>
        <el-form-item label="通道类型" required>
          <el-select v-model="form.channelType" style="width: 100%" @change="onChannelTypeChange">
            <el-option label="微信" value="wechat" />
            <el-option label="邮件" value="email" />
            <el-option label="短信" value="sms" />
          </el-select>
        </el-form-item>
        <el-form-item :label="targetLabel" required>
          <el-input v-model="form.target" :placeholder="targetPlaceholder" />
        </el-form-item>
        <el-form-item v-if="form.channelType === 'email'" label="SMTP服务器">
          <el-input v-model="form.smtpHost" placeholder="如：smtp.qq.com" />
        </el-form-item>
        <el-form-item v-if="form.channelType === 'email'" label="SMTP端口">
          <el-input v-model="form.smtpPort" placeholder="如：465" />
        </el-form-item>
        <el-form-item v-if="form.channelType === 'email'" label="授权密码">
          <el-input v-model="form.smtpPassword" type="password" placeholder="邮箱授权密码" show-password />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listNotificationConfigs,
  addNotificationConfig,
  updateNotificationConfig,
  deleteNotificationConfig,
  testNotification,
  updateQuietHours,
  getQuietHours,
} from '@/api/notification'

const configs = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  configId: null,
  configName: '',
  channelType: 'wechat',
  target: '',
  smtpHost: '',
  smtpPort: '',
  smtpPassword: '',
  status: 1,
})

const quietEnabled = ref(false)
const quietStart = ref(null)
const quietEnd = ref(null)

const channelTypeMap = { wechat: '微信', email: '邮件', sms: '短信' }

const channelTagType = (type) => {
  const map = { wechat: 'success', email: '', sms: 'warning' }
  return map[type] || ''
}

const targetLabel = computed(() => {
  const map = { wechat: 'Webhook地址', email: '邮箱地址', sms: '手机号码' }
  return map[form.channelType] || '接收目标'
})

const targetPlaceholder = computed(() => {
  const map = {
    wechat: '请输入企业微信Webhook地址',
    email: '请输入邮箱地址',
    sms: '请输入手机号码',
  }
  return map[form.channelType] || '请输入接收目标'
})

const onChannelTypeChange = () => {
  form.target = ''
}

const loadConfigs = async () => {
  try {
    const res = await listNotificationConfigs()
    configs.value = res.data || []
  } catch (e) {
    console.error('加载通知配置失败:', e)
  }
}

const loadQuietHours = async () => {
  try {
    const res = await getQuietHours()
    if (res.data) {
      quietEnabled.value = res.data.enabled || false
      quietStart.value = res.data.startTime ? new Date(`2000-01-01T${res.data.startTime}`) : null
      quietEnd.value = res.data.endTime ? new Date(`2000-01-01T${res.data.endTime}`) : null
    }
  } catch (e) {
    // no quiet hours set
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    configId: null,
    configName: '',
    channelType: 'wechat',
    target: '',
    smtpHost: '',
    smtpPort: '',
    smtpPassword: '',
    status: 1,
  })
  dialogVisible.value = true
}

const showEditDialog = (config) => {
  isEdit.value = true
  Object.assign(form, {
    configId: config.configId,
    configName: config.configName,
    channelType: config.channelType,
    target: config.target,
    smtpHost: config.smtpHost || '',
    smtpPort: config.smtpPort || '',
    smtpPassword: '',
    status: config.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.configName) {
    ElMessage.warning('请输入通道名称')
    return
  }
  if (!form.target) {
    ElMessage.warning('请输入接收目标')
    return
  }
  try {
    if (isEdit.value) {
      await updateNotificationConfig(form)
      ElMessage.success('更新成功')
    } else {
      await addNotificationConfig(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadConfigs()
  } catch (e) {
    // handled by interceptor
  }
}

const handleDelete = async (configId) => {
  try {
    await deleteNotificationConfig(configId)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (e) {
    // handled by interceptor
  }
}

const handleTest = async (config) => {
  try {
    await testNotification(config.configId)
    ElMessage.success('测试消息已发送')
  } catch (e) {
    // handled by interceptor
  }
}

const handleQuietToggle = (val) => {
  if (!val) {
    saveQuietHours()
  }
}

const saveQuietHours = async () => {
  try {
    const data = {
      enabled: quietEnabled.value,
      startTime: quietStart.value ? formatTime(quietStart.value) : null,
      endTime: quietEnd.value ? formatTime(quietEnd.value) : null,
    }
    await updateQuietHours(data)
    ElMessage.success('免打扰设置已保存')
  } catch (e) {
    // handled by interceptor
  }
}

const formatTime = (date) => {
  if (!date) return null
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

onMounted(() => {
  loadConfigs()
  loadQuietHours()
})
</script>

<style lang="scss" scoped>
.notification-page {
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #f0f4f8;
  }
}

.section-card {
  margin-bottom: 16px;
  background: rgba(15, 21, 53, 0.85) !important;
  border: 1px solid rgba(0, 212, 255, 0.12) !important;
  border-radius: 14px !important;

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 212, 255, 0.08);
    background: transparent;
  }

  :deep(.el-card__body) {
    padding: 20px;
    background: transparent;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #f0f4f8;

  .el-icon {
    color: #00d4ff;
  }
}

.empty-state {
  padding: 20px 0;

  :deep(.el-empty__description p) {
    color: #64748b;
  }
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 212, 255, 0.04);
    border-color: rgba(0, 212, 255, 0.15);
  }
}

.config-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;

  &--wechat {
    background: rgba(34, 197, 94, 0.12);
    color: #22c55e;
  }

  &--email {
    background: rgba(0, 212, 255, 0.12);
    color: #00d4ff;
  }

  &--sms {
    background: rgba(245, 158, 11, 0.12);
    color: #f59e0b;
  }
}

.config-body {
  flex: 1;
  min-width: 0;
}

.config-name {
  font-size: 14px;
  font-weight: 600;
  color: #f0f4f8;
  margin-bottom: 6px;
}

.config-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.config-target {
  font-size: 12px;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.config-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.quiet-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quiet-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quiet-label {
  font-size: 14px;
  color: #f0f4f8;
}

.quiet-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quiet-sep {
  color: #94a3b8;
  font-size: 14px;
}

.quiet-tip {
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #00d4ff;
  border-color: #00d4ff;
}

@media (max-width: 768px) {
  .config-item {
    flex-wrap: wrap;
  }

  .config-actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.04);
  }

  .quiet-time {
    flex-wrap: wrap;
  }
}
</style>
