<template>
  <div class="page-container">
    <div class="page-header">
      <h2>告警规则</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加规则
      </el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="rules" stripe>
        <el-table-column prop="alertName" label="规则名称" width="200" />
        <el-table-column label="设备" width="180">
          <template #default="{ row }">
            {{ getDeviceName(row.deviceId) }}
          </template>
        </el-table-column>
        <el-table-column prop="modelIdentifier" label="监测属性" width="150" />
        <el-table-column label="条件" width="200">
          <template #default="{ row }">
            {{ row.operator }} {{ row.threshold }}
          </template>
        </el-table-column>
        <el-table-column label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel === 3 ? 'danger' : row.alertLevel === 2 ? 'warning' : 'info'" size="small">
              {{ alertLevelMap[row.alertLevel] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyType" label="通知方式" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enable" :active-value="1" :inactive-value="0" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.alertId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '添加规则'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="规则名称" required>
          <el-input v-model="form.alertName" placeholder="如：温度过高告警" />
        </el-form-item>
        <el-form-item label="设备" required>
          <el-select v-model="form.deviceId" placeholder="选择设备" style="width: 100%" @change="onDeviceChange">
            <el-option v-for="d in allDevices" :key="d.deviceId" :label="d.deviceName" :value="d.deviceId" />
          </el-select>
        </el-form-item>
        <el-form-item label="监测属性" required>
          <el-select v-model="form.modelIdentifier" placeholder="选择属性" style="width: 100%">
            <el-option v-for="m in deviceModels" :key="m.identifier" :label="m.modelName" :value="m.identifier" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警条件" required>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-select v-model="form.operator" placeholder="运算符">
                <el-option label="大于 (>)" value=">" />
                <el-option label="小于 (<)" value="<" />
                <el-option label="等于 (=)" value="=" />
                <el-option label="大于等于 (>=)" value=">=" />
                <el-option label="小于等于 (<=)" value="<=" />
                <el-option label="不等于 (!=)" value="!=" />
              </el-select>
            </el-col>
            <el-col :span="16">
              <el-input v-model="form.threshold" placeholder="阈值" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="form.alertLevel" style="width: 100%">
            <el-option label="提示" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="form.notifyType" style="width: 100%">
            <el-option label="日志记录" value="log" />
            <el-option label="邮件" value="email" />
            <el-option label="短信" value="sms" />
            <el-option label="微信" value="wechat" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAlertRules, createAlertRule, updateAlertRule, deleteAlertRule } from '@/api/alert'
import { listDevices } from '@/api/device'
import { listThingsModels } from '@/api/product'

const rules = ref([])
const allDevices = ref([])
const deviceModels = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  alertId: null, alertName: '', deviceId: null, modelIdentifier: '',
  operator: '>', threshold: '', alertLevel: 2, notifyType: 'log',
})

const alertLevelMap = { 1: '提示', 2: '警告', 3: '严重' }

const getDeviceName = (id) => allDevices.value.find((d) => d.deviceId === id)?.deviceName || '-'

const loadData = async () => {
  const [rulesRes, devicesRes] = await Promise.all([listAlertRules(), listDevices({ pageSize: 1000 })])
  rules.value = rulesRes.data
  allDevices.value = devicesRes.data.rows
}

const onDeviceChange = async (deviceId) => {
  form.modelIdentifier = ''
  const device = allDevices.value.find((d) => d.deviceId === deviceId)
  if (device) {
    const res = await listThingsModels(device.productId)
    deviceModels.value = res.data.filter((m) => m.type === 1 || m.type === 3) // 属性和事件
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { alertId: null, alertName: '', deviceId: null, modelIdentifier: '', operator: '>', threshold: '', alertLevel: 2, notifyType: 'log' })
  deviceModels.value = []
  dialogVisible.value = true
}

const showEditDialog = async (row) => {
  isEdit.value = true
  Object.assign(form, row)
  await onDeviceChange(row.deviceId)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.alertName || !form.deviceId || !form.modelIdentifier || !form.threshold) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (isEdit.value) {
    await updateAlertRule(form)
    ElMessage.success('更新成功')
  } else {
    await createAlertRule(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleToggle = async (row) => {
  await updateAlertRule(row)
  ElMessage.success(row.enable === 1 ? '已启用' : '已禁用')
}

const handleDelete = async (alertId) => {
  await deleteAlertRule(alertId)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>
