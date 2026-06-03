<template>
  <div class="page-container">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <!-- 筛选 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="模块">
          <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="设备" value="设备" />
            <el-option label="场景" value="场景" />
            <el-option label="告警" value="告警" />
            <el-option label="系统" value="系统" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operator" placeholder="请输入操作人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>日志记录</span>
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="module" label="模块" width="90">
          <template #default="{ row }">
            <el-tag :type="moduleTagType[row.module]" size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="110" />
        <el-table-column prop="target" label="目标" min-width="160" show-overflow-tooltip />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { listLogs, exportLogs } from '@/api/log'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const dateRange = ref(null)

const queryParams = reactive({
  module: '',
  operator: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10,
})

const moduleTagType = {
  '设备': '',
  '场景': 'success',
  '告警': 'warning',
  '系统': 'info',
}

const loadLogs = async () => {
  loading.value = true
  try {
    // 同步日期范围到查询参数
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    const res = await listLogs(queryParams)
    logs.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadLogs()
}

const handleReset = () => {
  queryParams.module = ''
  queryParams.operator = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = null
  queryParams.pageNum = 1
  loadLogs()
}

const handleExport = async () => {
  try {
    const params = { ...queryParams }
    delete params.pageNum
    delete params.pageSize
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await exportLogs(params)
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => loadLogs())
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #e8ecf1;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
