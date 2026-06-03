<template>
  <div class="page-container">
    <div class="page-header">
      <h2>告警日志</h2>
    </div>

    <!-- 筛选 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="未处理" :value="1" />
            <el-option label="已处理" :value="2" />
            <el-option label="已忽略" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="queryParams.level" placeholder="全部级别" clearable style="width: 120px">
            <el-option label="提示" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLogs">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="logs" stripe style="width: 100%">
        <el-table-column label="告警级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel === 3 ? 'danger' : row.alertLevel === 2 ? 'warning' : 'info'" size="small">
              {{ alertLevelMap[row.alertLevel] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备" min-width="120" />
        <el-table-column prop="alertMessage" label="告警消息" min-width="250" show-overflow-tooltip />
        <el-table-column prop="alertValue" label="告警值" width="90" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="告警时间" min-width="160" />
        <el-table-column label="操作" min-width="140" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <template v-if="row.status === 1">
                <el-button text type="success" size="small" @click="handleAlert(row.logId)">处理</el-button>
                <el-button text type="info" size="small" @click="ignoreAlertLog(row.logId)">忽略</el-button>
              </template>
              <span v-else style="color: #94a3b8; font-size: 12px">
                {{ row.handleRemark || '已处理' }}
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAlertLogs, handleAlert as handleAlertApi, ignoreAlert as ignoreAlertApi } from '@/api/alert'

const queryParams = reactive({ status: null, level: null, pageNum: 1, pageSize: 10 })
const logs = ref([])
const total = ref(0)

const alertLevelMap = { 1: '提示', 2: '警告', 3: '严重' }
const statusMap = { 1: '未处理', 2: '已处理', 3: '已忽略' }
const statusTagType = { 1: 'danger', 2: 'success', 3: 'info' }

const loadLogs = async () => {
  const res = await listAlertLogs(queryParams)
  logs.value = res.data.rows
  total.value = res.data.total
}

const handleAlert = async (logId) => {
  const { value } = await ElMessageBox.prompt('处理备注', '处理告警', { inputType: 'textarea' }).catch(() => ({ value: null }))
  if (value === null) return
  await handleAlertApi(logId, value)
  ElMessage.success('已处理')
  loadLogs()
}

const ignoreAlertLog = async (logId) => {
  await ignoreAlertApi(logId, '已忽略')
  ElMessage.success('已忽略')
  loadLogs()
}

onMounted(() => loadLogs())
</script>

<style lang="scss" scoped>
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
  flex-wrap: nowrap;
}
</style>
