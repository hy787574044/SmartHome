<template>
  <div class="page-container">
    <div class="page-header">
      <h2>场景联动</h2>
      <div class="header-actions">
        <el-button @click="$router.push('/scene/templates')">
          <el-icon><Connection /></el-icon> 从模板创建
        </el-button>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 创建场景
        </el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="scenes" stripe>
        <el-table-column prop="sceneName" label="场景名称" width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.sceneType === 1 ? '' : 'success'" size="small">
              {{ row.sceneType === 1 ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="条件关系" width="100">
          <template #default="{ row }">
            {{ row.conditionType === 2 ? '全部满足' : '任一满足' }}
          </template>
        </el-table-column>
        <el-table-column label="静默期" width="100">
          <template #default="{ row }">
            {{ row.silentPeriod ? `${row.silentPeriod}分钟` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enable" :active-value="1" :inactive-value="0" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button text type="success" size="small" @click="handleExecute(row)">执行</el-button>
            <el-button text type="primary" size="small" @click="showDetailDialog(row)">详情</el-button>
            <el-button text type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="handleCopy(row)">复制</el-button>
            <el-button text type="info" size="small" @click="showLogDialog(row)">日志</el-button>
            <el-popconfirm title="确定删除该场景？" @confirm="handleDelete(row.sceneId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑场景对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑场景' : '创建场景'" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="场景名称" required>
          <el-input v-model="form.scene.sceneName" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景类型">
          <el-radio-group v-model="form.scene.sceneType">
            <el-radio :value="1">手动</el-radio>
            <el-radio :value="2">自动</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scene.sceneType === 2" label="条件关系">
          <el-radio-group v-model="form.scene.conditionType">
            <el-radio :value="1">任一满足 (OR)</el-radio>
            <el-radio :value="2">全部满足 (AND)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="静默期">
          <el-input-number v-model="form.scene.silentPeriod" :min="0" :max="1440" />
          <span style="margin-left: 8px; color: #94a3b8">分钟（防止重复触发）</span>
        </el-form-item>

        <!-- 触发条件 -->
        <el-divider>触发条件</el-divider>
        <div v-for="(trigger, index) in form.triggers" :key="index" class="trigger-item">
          <el-row :gutter="12">
            <el-col :span="6">
              <el-select v-model="trigger.triggerType" placeholder="触发类型">
                <el-option label="设备触发" :value="1" />
                <el-option label="定时触发" :value="2" />
                <el-option label="条件触发" :value="3" />
              </el-select>
            </el-col>
            <el-col :span="6" v-if="trigger.triggerType !== 2">
              <el-select v-model="trigger.deviceId" placeholder="选择设备" @change="onTriggerDeviceChange(trigger)">
                <el-option v-for="d in allDevices" :key="d.deviceId" :label="d.deviceName" :value="d.deviceId" />
              </el-select>
            </el-col>
            <el-col :span="6" v-if="trigger.triggerType !== 2">
              <el-select v-model="trigger.modelIdentifier" placeholder="选择属性">
                <el-option v-for="m in getDeviceModels(trigger.deviceId)" :key="m.identifier" :label="m.modelName" :value="m.identifier" />
              </el-select>
            </el-col>
            <el-col :span="4" v-if="trigger.triggerType !== 2">
              <el-select v-model="trigger.operator" placeholder="运算符">
                <el-option label="=" value="=" />
                <el-option label="!=" value="!=" />
                <el-option label=">" value=">" />
                <el-option label="<" value="<" />
                <el-option label=">=" value=">=" />
                <el-option label="<=" value="<=" />
              </el-select>
            </el-col>
            <el-col :span="4" v-if="trigger.triggerType !== 2">
              <el-input v-model="trigger.value" placeholder="值" />
            </el-col>
            <el-col :span="8" v-if="trigger.triggerType === 2">
              <el-input v-model="trigger.cronExpression" placeholder="cron表达式，如: 0 0 8 * * ?" />
            </el-col>
            <el-col :span="2">
              <el-button text type="danger" @click="form.triggers.splice(index, 1)">删除</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" text @click="addTrigger">+ 添加触发条件</el-button>

        <!-- 执行动作 -->
        <el-divider>执行动作</el-divider>
        <div v-for="(action, index) in form.actions" :key="index" class="action-item">
          <el-row :gutter="12">
            <el-col :span="5">
              <el-select v-model="action.actionType" placeholder="动作类型">
                <el-option label="设备控制" :value="1" />
                <el-option label="告警通知" :value="2" />
              </el-select>
            </el-col>
            <el-col :span="6" v-if="action.actionType === 1">
              <el-select v-model="action.deviceId" placeholder="选择设备" @change="onActionDeviceChange(action)">
                <el-option v-for="d in allDevices" :key="d.deviceId" :label="d.deviceName" :value="d.deviceId" />
              </el-select>
            </el-col>
            <el-col :span="5" v-if="action.actionType === 1">
              <el-select v-model="action.modelIdentifier" placeholder="选择功能">
                <el-option v-for="m in getDeviceFunctions(action.deviceId)" :key="m.identifier" :label="m.modelName" :value="m.identifier" />
              </el-select>
            </el-col>
            <el-col :span="4" v-if="action.actionType === 1">
              <el-input v-model="action.value" placeholder="值" />
            </el-col>
            <el-col :span="4">
              <el-input-number v-model="action.delaySeconds" :min="0" :max="3600" placeholder="延迟(秒)" size="small" />
            </el-col>
            <el-col :span="2">
              <el-button text type="danger" @click="form.actions.splice(index, 1)">删除</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" text @click="addAction">+ 添加执行动作</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 场景详情对话框 -->
    <el-dialog v-model="detailVisible" :title="`场景详情 - ${detailScene?.sceneName}`" width="600px">
      <h4>触发条件</h4>
      <el-table :data="detailTriggers" stripe size="small" style="margin-bottom: 16px">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ triggerTypeMap[row.triggerType] }}</template>
        </el-table-column>
        <el-table-column prop="modelIdentifier" label="标识符" />
        <el-table-column prop="operator" label="运算符" width="80" />
        <el-table-column prop="value" label="值" width="100" />
        <el-table-column prop="cronExpression" label="Cron" />
      </el-table>
      <h4>执行动作</h4>
      <el-table :data="detailActions" stripe size="small">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ row.actionType === 1 ? '设备控制' : '告警通知' }}</template>
        </el-table-column>
        <el-table-column prop="modelIdentifier" label="标识符" />
        <el-table-column prop="value" label="值" width="100" />
        <el-table-column label="延迟" width="100">
          <template #default="{ row }">{{ row.delaySeconds ? `${row.delaySeconds}秒` : '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 执行日志对话框 -->
    <el-dialog v-model="logVisible" :title="`执行日志 - ${logScene?.sceneName}`" width="750px">
      <el-table :data="sceneLogs" stripe size="small" v-loading="logLoading">
        <el-table-column prop="executeTime" label="执行时间" width="180">
          <template #default="{ row }">
            {{ row.executeTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="触发方式" width="100">
          <template #default="{ row }">
            {{ row.triggerType === 1 ? '手动' : row.triggerType === 2 ? '定时' : '自动' }}
          </template>
        </el-table-column>
        <el-table-column label="执行结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="详情" show-overflow-tooltip />
      </el-table>
      <div v-if="sceneLogs.length === 0 && !logLoading" class="log-empty">
        <el-empty description="暂无执行记录" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listScenes, createScene, updateScene, deleteScene, executeScene, listTriggers, listActions, copyScene, getSceneLogs } from '@/api/scene'
import { listDevices } from '@/api/device'
import { listThingsModels } from '@/api/product'

const route = useRoute()
const scenes = ref([])
const allDevices = ref([])
const deviceModelCache = ref({})

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  scene: { sceneName: '', sceneType: 1, conditionType: 1, enable: 1, silentPeriod: 0 },
  triggers: [],
  actions: [],
})

const detailVisible = ref(false)
const detailScene = ref(null)
const detailTriggers = ref([])
const detailActions = ref([])

const logVisible = ref(false)
const logScene = ref(null)
const sceneLogs = ref([])
const logLoading = ref(false)

const triggerTypeMap = { 1: '设备触发', 2: '定时触发', 3: '条件触发' }

const loadScenes = async () => {
  const res = await listScenes()
  scenes.value = res.data
}

const getDeviceModels = (deviceId) => {
  if (!deviceId) return []
  return deviceModelCache.value[deviceId] || []
}

const getDeviceFunctions = (deviceId) => {
  if (!deviceId) return []
  return (deviceModelCache.value[deviceId] || []).filter((m) => m.type === 2)
}

const onTriggerDeviceChange = async (trigger) => {
  trigger.modelIdentifier = ''
  if (!deviceModelCache.value[trigger.deviceId]) {
    const device = allDevices.value.find((d) => d.deviceId === trigger.deviceId)
    if (device) {
      const res = await listThingsModels(device.productId)
      deviceModelCache.value[trigger.deviceId] = res.data
    }
  }
}

const onActionDeviceChange = async (action) => {
  action.modelIdentifier = ''
  if (!deviceModelCache.value[action.deviceId]) {
    const device = allDevices.value.find((d) => d.deviceId === action.deviceId)
    if (device) {
      const res = await listThingsModels(device.productId)
      deviceModelCache.value[action.deviceId] = res.data
    }
  }
}

const addTrigger = () => {
  form.triggers.push({ triggerType: 1, deviceId: null, modelIdentifier: '', operator: '=', value: '', cronExpression: '' })
}

const addAction = () => {
  form.actions.push({ actionType: 1, deviceId: null, modelIdentifier: '', value: '', delaySeconds: 0 })
}

const showAddDialog = () => {
  isEdit.value = false
  form.scene = { sceneName: '', sceneType: 1, conditionType: 1, enable: 1, silentPeriod: 0 }
  form.triggers = [{ triggerType: 1, deviceId: null, modelIdentifier: '', operator: '=', value: '', cronExpression: '' }]
  form.actions = [{ actionType: 1, deviceId: null, modelIdentifier: '', value: '', delaySeconds: 0 }]
  dialogVisible.value = true
}

// Open create dialog prefilled from template data
const showTemplateDialog = (template) => {
  isEdit.value = false
  form.scene = {
    sceneName: template.templateName || '',
    sceneType: 1,
    conditionType: 1,
    enable: 1,
    silentPeriod: 0,
  }
  form.triggers = (template.triggers || []).map((t) => ({
    triggerType: t.triggerType || 1,
    deviceId: t.deviceId || null,
    modelIdentifier: t.modelIdentifier || '',
    operator: t.operator || '=',
    value: t.value || '',
    cronExpression: t.cronExpression || '',
  }))
  form.actions = (template.actions || []).map((a) => ({
    actionType: a.actionType || 1,
    deviceId: a.deviceId || null,
    modelIdentifier: a.modelIdentifier || '',
    value: a.value || '',
    delaySeconds: a.delaySeconds || 0,
  }))
  // Ensure at least one trigger and one action
  if (form.triggers.length === 0) {
    form.triggers.push({ triggerType: 1, deviceId: null, modelIdentifier: '', operator: '=', value: '', cronExpression: '' })
  }
  if (form.actions.length === 0) {
    form.actions.push({ actionType: 1, deviceId: null, modelIdentifier: '', value: '', delaySeconds: 0 })
  }
  dialogVisible.value = true
}

const showEditDialog = async (row) => {
  isEdit.value = true
  form.scene = { ...row }
  const [triggersRes, actionsRes] = await Promise.all([listTriggers(row.sceneId), listActions(row.sceneId)])
  form.triggers = triggersRes.data
  form.actions = actionsRes.data
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.scene.sceneName) {
    ElMessage.warning('请输入场景名称')
    return
  }
  if (form.triggers.length === 0) {
    ElMessage.warning('至少需要一个触发条件')
    return
  }
  if (form.actions.length === 0) {
    ElMessage.warning('至少需要一个执行动作')
    return
  }
  await createScene(form)
  ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
  dialogVisible.value = false
  loadScenes()
}

const handleToggle = async (row) => {
  await updateScene(row)
  ElMessage.success(row.enable === 1 ? '已启用' : '已禁用')
}

const handleExecute = async (row) => {
  await executeScene(row.sceneId)
  ElMessage.success('场景已执行')
}

const handleDelete = async (sceneId) => {
  await deleteScene(sceneId)
  ElMessage.success('删除成功')
  loadScenes()
}

const handleCopy = async (row) => {
  try {
    await copyScene(row.sceneId)
    ElMessage.success('场景复制成功')
    loadScenes()
  } catch (e) {
    // handled by interceptor
  }
}

const showDetailDialog = async (row) => {
  detailScene.value = row
  detailVisible.value = true
  const [triggersRes, actionsRes] = await Promise.all([listTriggers(row.sceneId), listActions(row.sceneId)])
  detailTriggers.value = triggersRes.data
  detailActions.value = actionsRes.data
}

const showLogDialog = async (row) => {
  logScene.value = row
  logVisible.value = true
  logLoading.value = true
  sceneLogs.value = []
  try {
    const res = await getSceneLogs({ sceneId: row.sceneId, pageSize: 50 })
    sceneLogs.value = res.data?.rows || res.data || []
  } catch (e) {
    console.error('加载执行日志失败:', e)
  } finally {
    logLoading.value = false
  }
}

onMounted(async () => {
  const devicesRes = await listDevices({ pageSize: 1000 })
  allDevices.value = devicesRes.data.rows
  loadScenes()

  // Check if navigated from template page with prefilled data
  const templateData = history.state?.templateData
  if (route.query.fromTemplate === '1' && templateData) {
    await nextTick()
    showTemplateDialog(templateData)
  }
})
</script>

<style lang="scss" scoped>
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

.header-actions {
  display: flex;
  gap: 12px;
}

.trigger-item, .action-item {
  padding: 16px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
}

.log-empty {
  padding: 20px 0;
}
</style>
