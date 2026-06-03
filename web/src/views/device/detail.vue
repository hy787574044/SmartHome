<template>
  <div class="device-detail">
    <!-- 顶部：设备名称、状态标签、所属房间、产品名称 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="device-name">{{ deviceInfo.deviceName || '设备详情' }}</h2>
        <el-tag
          :type="deviceInfo.status === 3 ? 'success' : deviceInfo.status === 4 ? 'info' : 'warning'"
          size="small"
          effect="dark"
          class="status-tag"
        >
          {{ statusMap[deviceInfo.status] || '未知' }}
        </el-tag>
      </div>
      <div class="header-meta">
        <span class="meta-item" v-if="deviceInfo.roomName">
          <el-icon><House /></el-icon> {{ deviceInfo.roomName }}
        </span>
        <span class="meta-item" v-if="deviceInfo.productName">
          <el-icon><Goods /></el-icon> {{ deviceInfo.productName }}
        </span>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else>
      <!-- 控制区域：根据物模型类型显示控件 -->
      <el-card v-if="controlFunctions.length > 0" shadow="hover" class="section-card">
        <template #header>
          <div class="card-header">
            <el-icon><Operation /></el-icon>
            <span>设备控制</span>
          </div>
        </template>
        <div class="control-grid">
          <div v-for="func in controlFunctions" :key="func.identifier" class="control-item">
            <div class="control-info">
              <span class="control-name">{{ func.modelName }}</span>
              <span class="control-id">{{ func.identifier }}</span>
            </div>
            <div class="control-action">
              <!-- bool 类型：开关 -->
              <el-switch
                v-if="func.dataType === 'bool'"
                v-model="controlValues[func.identifier]"
                active-value="1"
                inactive-value="0"
                active-text="开"
                inactive-text="关"
                @change="(val) => handleControl(func.identifier, val)"
              />
              <!-- integer / decimal 类型：滑块 -->
              <div
                v-else-if="func.dataType === 'integer' || func.dataType === 'decimal'"
                class="slider-wrap"
              >
                <el-slider
                  v-model="controlValues[func.identifier]"
                  :min="Number(func.min) || 0"
                  :max="Number(func.max) || 100"
                  :step="func.dataType === 'decimal' ? 0.1 : 1"
                  :show-tooltip="true"
                  @change="(val) => handleControl(func.identifier, val)"
                />
                <span class="slider-val">{{ controlValues[func.identifier] }}{{ func.unit || '' }}</span>
              </div>
              <!-- string 类型：输入框 -->
              <div v-else class="input-wrap">
                <el-input
                  v-model="controlValues[func.identifier]"
                  :placeholder="`输入${func.modelName}`"
                  style="width: 140px; margin-right: 8px"
                  clearable
                />
                <el-button
                  type="primary"
                  size="small"
                  @click="handleControl(func.identifier, controlValues[func.identifier])"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 属性展示：卡片式展示当前属性值 -->
      <el-card v-if="properties.length > 0" shadow="hover" class="section-card">
        <template #header>
          <div class="card-header">
            <el-icon><DataLine /></el-icon>
            <span>当前属性</span>
          </div>
        </template>
        <div class="props-grid">
          <div
            v-for="prop in properties"
            :key="prop.identifier"
            class="prop-card"
            @click="handlePropClick(prop)"
          >
            <div class="prop-icon">
              <el-icon :size="24"><Cpu /></el-icon>
            </div>
            <div class="prop-body">
              <div class="prop-value">
                {{ prop.value ?? '-' }}
                <span class="prop-unit">{{ prop.unit || '' }}</span>
              </div>
              <div class="prop-name">{{ prop.modelName }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 历史数据：ECharts 图表 -->
      <el-card shadow="hover" class="section-card">
        <template #header>
          <div class="card-header">
            <el-icon><TrendCharts /></el-icon>
            <span>历史数据</span>
          </div>
        </template>
        <div class="history-controls">
          <el-select
            v-model="historyProperty"
            placeholder="选择属性"
            style="width: 160px"
            @change="loadHistory"
          >
            <el-option
              v-for="prop in properties"
              :key="prop.identifier"
              :label="prop.modelName"
              :value="prop.identifier"
            />
          </el-select>
          <el-radio-group v-model="historyPreset" @change="handlePresetChange" class="preset-group">
            <el-radio-button label="24h">近24小时</el-radio-button>
            <el-radio-button label="7d">近7天</el-radio-button>
            <el-radio-button label="30d">近30天</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-if="historyPreset === 'custom'"
            v-model="historyRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="margin-left: 4px"
            @change="loadHistory"
          />
        </div>
        <div ref="chartRef" class="chart-container"></div>
        <el-empty v-if="!historyLoading && chartEmpty" description="暂无历史数据" :image-size="80" />
      </el-card>

      <!-- 设备信息 -->
      <el-card shadow="hover" class="section-card">
        <template #header>
          <div class="card-header">
            <el-icon><Monitor /></el-icon>
            <span>设备信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">{{ deviceInfo.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="序列号">{{ deviceInfo.serialNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属产品">{{ deviceInfo.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属房间">{{ deviceInfo.roomName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备状态">
            <el-tag :type="deviceInfo.status === 3 ? 'success' : 'info'" size="small">
              {{ statusMap[deviceInfo.status] || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后上线">{{ deviceInfo.lastOnlineTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ deviceInfo.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ deviceInfo.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDeviceDetail, controlDevice, getDeviceHistory } from '@/api/device'
import { listThingsModels } from '@/api/product'
import * as echarts from 'echarts'

const route = useRoute()
const deviceId = route.params.deviceId

const loading = ref(true)
const deviceInfo = ref({})
const properties = ref([])
const controlFunctions = ref([])
const controlValues = reactive({})

// 历史数据相关
const historyProperty = ref('')
const historyPreset = ref('24h')
const historyRange = ref([])
const historyLoading = ref(false)
const chartEmpty = ref(false)
const chartRef = ref(null)
let chartInstance = null

const statusMap = { 1: '未激活', 2: '禁用', 3: '在线', 4: '离线' }

/** 根据预设选项计算起止时间 */
const calcTimeRange = (preset) => {
  const now = new Date()
  const end = now
  let start
  switch (preset) {
    case '24h':
      start = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      break
    case '7d':
      start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    case '30d':
      start = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      break
    default:
      return null
  }
  return {
    startTime: formatDate(start),
    endTime: formatDate(end),
  }
}

const formatDate = (d) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const handlePresetChange = (val) => {
  if (val !== 'custom') {
    historyRange.value = []
  }
  loadHistory()
}

/** 点击属性卡片，自动切换到该属性的历史曲线 */
const handlePropClick = (prop) => {
  historyProperty.value = prop.identifier
  loadHistory()
}

const loadDeviceDetail = async () => {
  try {
    const res = await getDeviceDetail(deviceId)
    deviceInfo.value = res.data

    // 加载物模型
    const modelsRes = await listThingsModels(deviceInfo.value.productId)
    const models = modelsRes.data || []

    // 区分属性（type 1）和功能（type 2）
    properties.value = models.filter((m) => m.type === 1)
    controlFunctions.value = models.filter((m) => m.type === 2)

    // 初始化控件默认值
    controlFunctions.value.forEach((f) => {
      if (f.dataType === 'bool') {
        controlValues[f.identifier] = '0'
      } else if (f.dataType === 'integer' || f.dataType === 'decimal') {
        controlValues[f.identifier] = Number(f.min) || 0
      } else {
        controlValues[f.identifier] = ''
      }
    })

    // 用详情中的当前值填充属性卡片
    if (deviceInfo.value.properties) {
      properties.value.forEach((p) => {
        if (deviceInfo.value.properties[p.identifier] !== undefined) {
          p.value = deviceInfo.value.properties[p.identifier]
        }
      })
    }

    // 默认展示第一个属性的近24小时历史
    if (properties.value.length > 0) {
      historyProperty.value = properties.value[0].identifier
      await loadHistory()
    }
  } catch (e) {
    console.error('加载设备详情失败:', e)
  } finally {
    loading.value = false
  }
}

const handleControl = async (identifier, value) => {
  try {
    await controlDevice(deviceId, identifier, value)
    ElMessage.success('指令已发送')
  } catch (e) {
    // interceptor handles the error
  }
}

const loadHistory = async () => {
  if (!historyProperty.value) return
  historyLoading.value = true
  chartEmpty.value = false

  try {
    const params = { identifier: historyProperty.value }

    // 自定义日期范围
    if (historyPreset.value === 'custom' && historyRange.value && historyRange.value.length === 2) {
      params.startTime = historyRange.value[0]
      params.endTime = historyRange.value[1]
    } else {
      // 预设时间范围
      const range = calcTimeRange(historyPreset.value)
      if (range) {
        params.startTime = range.startTime
        params.endTime = range.endTime
      }
    }

    const res = await getDeviceHistory(deviceId, params)
    const data = res.data || []
    chartEmpty.value = data.length === 0
    if (data.length > 0) {
      renderChart(data)
    } else {
      // 清空图表
      chartInstance?.clear()
    }
  } catch (e) {
    console.error('加载历史数据失败:', e)
  } finally {
    historyLoading.value = false
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return

  await nextTick()

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const times = data.map((d) => d.time || d.createTime)
  const values = data.map((d) => d.value)

  // 找到当前选中属性的单位
  const currentProp = properties.value.find((p) => p.identifier === historyProperty.value)
  const unit = currentProp?.unit || ''

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(40, 48, 69, 0.95)',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      textStyle: { color: '#f0f4f8' },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>${p.marker} ${currentProp?.modelName || ''}: ${p.value}${unit}`
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 11,
        formatter: (val) => {
          // 仅显示时间部分，缩短标签
          if (typeof val === 'string' && val.length > 10) {
            return val.substring(11, 16)
          }
          return val
        },
      },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      name: unit,
      nameTextStyle: { color: '#94a3b8', fontSize: 11 },
      axisLine: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100,
      },
    ],
    series: [
      {
        data: values,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#00d4ff', width: 2 },
        itemStyle: { color: '#00d4ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 212, 255, 0.35)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.02)' },
          ]),
        },
      },
    ],
  }
  chartInstance.setOption(option, true)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  loadDeviceDetail()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style lang="scss" scoped>
.device-detail {
  min-height: calc(100vh - 100px);
}

/* ---- 顶部 header ---- */
.page-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .device-name {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    color: #f0f4f8;
  }

  .back-btn {
    color: #00d4ff;
    font-size: 14px;
    &:hover {
      color: #33ddff;
    }
  }
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-left: 58px;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #94a3b8;

    .el-icon {
      color: #00d4ff;
    }
  }
}

/* ---- 通用 ---- */
.loading-wrap {
  padding: 24px;
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

/* ---- 控制区域 ---- */
.control-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 212, 255, 0.04);
    border-color: rgba(0, 212, 255, 0.15);
  }
}

.control-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.control-name {
  font-size: 14px;
  font-weight: 500;
  color: #f0f4f8;
}

.control-id {
  font-size: 12px;
  color: #64748b;
}

.control-action {
  display: flex;
  align-items: center;
}

.slider-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 260px;
}

.slider-val {
  font-size: 14px;
  font-weight: 600;
  color: #00d4ff;
  min-width: 40px;
  text-align: right;
}

.input-wrap {
  display: flex;
  align-items: center;
}

/* ---- 属性卡片 ---- */
.props-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.prop-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 212, 255, 0.04);
    border-color: rgba(0, 212, 255, 0.15);
    transform: translateY(-2px);
  }
}

.prop-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(0, 212, 255, 0.1);
  color: #00d4ff;
  flex-shrink: 0;
}

.prop-body {
  flex: 1;
  min-width: 0;
}

.prop-value {
  font-size: 22px;
  font-weight: 700;
  color: #f0f4f8;
  line-height: 1.2;
}

.prop-unit {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 2px;
}

.prop-name {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

/* ---- 历史数据 ---- */
.history-controls {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-group {
  margin-left: 8px;

  :deep(.el-radio-button__inner) {
    background: rgba(255, 255, 255, 0.04);
    border-color: rgba(0, 212, 255, 0.15);
    color: #94a3b8;
    font-size: 12px;
    padding: 6px 12px;

    &:hover {
      color: #00d4ff;
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: rgba(0, 212, 255, 0.15);
    border-color: rgba(0, 212, 255, 0.4);
    color: #00d4ff;
    box-shadow: -1px 0 0 0 rgba(0, 212, 255, 0.4);
  }
}

.chart-container {
  width: 100%;
  height: 350px;
}

/* ---- 设备信息描述 ---- */
:deep(.el-descriptions) {
  background: transparent;
}

:deep(.el-descriptions__label) {
  background: rgba(0, 212, 255, 0.06) !important;
  color: #94a3b8 !important;
  border-color: rgba(255, 255, 255, 0.06) !important;
}

:deep(.el-descriptions__content) {
  background: transparent !important;
  color: #f0f4f8 !important;
  border-color: rgba(255, 255, 255, 0.06) !important;
}

/* ---- Element Plus 主题覆盖 ---- */
:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #00d4ff;
  border-color: #00d4ff;
}

:deep(.el-slider__bar) {
  background-color: #00d4ff;
}

:deep(.el-slider__button) {
  border-color: #00d4ff;
}

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .header-meta {
    padding-left: 0;
  }

  .props-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 250px;
  }

  .slider-wrap {
    width: 180px;
  }
}
</style>
