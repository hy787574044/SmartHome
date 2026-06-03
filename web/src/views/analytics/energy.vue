<template>
  <div class="energy">
    <!-- Decorative background -->
    <div class="grid-bg"></div>
    <div class="glow-orb glow-orb-1"></div>
    <div class="glow-orb glow-orb-2"></div>

    <!-- Top Stat Cards -->
    <div class="stat-row">
      <div
        v-for="(card, idx) in statCards"
        :key="idx"
        class="stat-card"
        :class="`stat-card--${card.theme}`"
      >
        <div class="stat-card__glow"></div>
        <div class="stat-card__icon">
          <el-icon :size="28"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">
            <span class="count-num">{{ animatedValues[idx] }}</span>
            <span class="stat-card__unit">{{ card.unit }}</span>
          </div>
          <div class="stat-card__label">{{ card.label }}</div>
        </div>
        <div class="stat-card__trend" :class="card.trendUp ? 'trend-up' : 'trend-down'">
          <el-icon :size="14">
            <component :is="card.trendUp ? 'Top' : 'Bottom'" />
          </el-icon>
          <span>{{ card.trend }}</span>
        </div>
      </div>
    </div>

    <!-- Charts Row 1: Trend + Device Ranking -->
    <div class="chart-row">
      <!-- Energy Trend -->
      <div class="panel panel--trend">
        <div class="panel__header">
          <div class="panel__title">
            <span class="panel__title-icon">
              <el-icon><TrendCharts /></el-icon>
            </span>
            用电趋势
          </div>
          <el-radio-group v-model="trendMode" size="small" class="trend-switch" @change="updateTrendChart">
            <el-radio-button value="day">日</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
            <el-radio-button value="month">月</el-radio-button>
          </el-radio-group>
        </div>
        <div class="panel__body">
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </div>

      <!-- Device Ranking -->
      <div class="panel panel--ranking">
        <div class="panel__header">
          <div class="panel__title">
            <span class="panel__title-icon panel__title-icon--orange">
              <el-icon><Histogram /></el-icon>
            </span>
            设备用电排行 Top10
          </div>
        </div>
        <div class="panel__body">
          <div ref="rankingChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- Charts Row 2: Room Distribution -->
    <div class="chart-row chart-row--single">
      <div class="panel panel--pie">
        <div class="panel__header">
          <div class="panel__title">
            <span class="panel__title-icon panel__title-icon--purple">
              <el-icon><PieChartIcon /></el-icon>
            </span>
            房间用电分布
          </div>
        </div>
        <div class="panel__body">
          <div ref="pieChartRef" class="chart-container chart-container--pie"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  Lightning, TrendCharts, Histogram,
  PieChart as PieChartIcon, Top, Bottom, DataLine
} from '@element-plus/icons-vue'

// ===== Stat Cards =====
const statCards = [
  {
    label: '今日用电',
    value: 28.6,
    unit: 'kWh',
    icon: Lightning,
    theme: 'cyan',
    trend: '3.2%',
    trendUp: true,
  },
  {
    label: '本周用电',
    value: 185.4,
    unit: 'kWh',
    icon: DataLine,
    theme: 'blue',
    trend: '5.8%',
    trendUp: true,
  },
  {
    label: '本月用电',
    value: 742.3,
    unit: 'kWh',
    icon: TrendCharts,
    theme: 'green',
    trend: '2.1%',
    trendUp: false,
  },
  {
    label: '环比变化',
    value: -2.1,
    unit: '%',
    icon: Histogram,
    theme: 'orange',
    trend: '下降',
    trendUp: false,
  },
]

const animatedValues = reactive([0, 0, 0, 0])

// ===== Chart Refs =====
const trendChartRef = ref(null)
const rankingChartRef = ref(null)
const pieChartRef = ref(null)
const trendMode = ref('day')

let trendChart = null
let rankingChart = null
let pieChart = null

// ===== Chart Theme Colors =====
const colors = {
  cyan: '#00f0ff',
  blue: '#3b82f6',
  purple: '#8b5cf6',
  green: '#22c55e',
  orange: '#f59e0b',
  red: '#ef4444',
  pink: '#ec4899',
  indigo: '#6366f1',
  teal: '#14b8a6',
  lime: '#84cc16',
  textPrimary: '#e2e8f0',
  textSecondary: '#94a3b8',
  textDim: '#475569',
  bgTooltip: 'rgba(15, 21, 53, 0.95)',
  borderGlow: 'rgba(0, 240, 255, 0.15)',
}

// ===== Mock Data =====
const trendData = {
  day: {
    xAxis: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
    series: [0.8, 0.5, 0.3, 0.6, 2.1, 3.5, 4.2, 3.8, 3.2, 4.8, 3.5, 1.8],
  },
  week: {
    xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series: [25.2, 28.6, 22.8, 30.1, 27.5, 35.2, 32.8],
  },
  month: {
    xAxis: ['1日', '5日', '10日', '15日', '20日', '25日', '30日'],
    series: [22.5, 26.8, 30.2, 28.5, 32.1, 29.8, 25.6],
  },
}

const deviceRanking = [
  { name: '中央空调', value: 156.8 },
  { name: '电热水器', value: 98.5 },
  { name: '洗衣机', value: 45.2 },
  { name: '冰箱', value: 38.6 },
  { name: '电视', value: 32.1 },
  { name: '烘干机', value: 28.5 },
  { name: '洗碗机', value: 22.3 },
  { name: '微波炉', value: 15.8 },
  { name: '烤箱', value: 12.4 },
  { name: '空气净化器', value: 8.6 },
]

const roomDistribution = [
  { name: '客厅', value: 285.6 },
  { name: '主卧', value: 168.3 },
  { name: '厨房', value: 142.8 },
  { name: '次卧', value: 86.5 },
  { name: '卫生间', value: 52.4 },
  { name: '书房', value: 35.2 },
]

// ===== Init Charts =====
const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChart) return
  const data = trendData[trendMode.value]
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.bgTooltip,
      borderColor: colors.borderGlow,
      borderWidth: 1,
      textStyle: { color: colors.textPrimary, fontSize: 13 },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: colors.textDim },
        lineStyle: { color: colors.cyan, type: 'dashed' },
      },
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-weight:600;margin-bottom:4px">${p.name}</div>
                <div style="display:flex;align-items:center;gap:6px">
                  <span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${colors.cyan}"></span>
                  用电量：<span style="color:${colors.cyan};font-weight:700">${p.value}</span> kWh
                </div>`
      },
    },
    grid: {
      left: 50,
      right: 20,
      top: 30,
      bottom: 30,
    },
    xAxis: {
      type: 'category',
      data: data.xAxis,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
      axisTick: { show: false },
      axisLabel: { color: colors.textSecondary, fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      name: 'kWh',
      nameTextStyle: { color: colors.textDim, fontSize: 11, padding: [0, 40, 0, 0] },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.textSecondary, fontSize: 11 },
    },
    series: [
      {
        type: 'line',
        data: data.series,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors.cyan },
            { offset: 1, color: colors.blue },
          ]),
        },
        itemStyle: {
          color: colors.cyan,
          borderWidth: 2,
          borderColor: '#0a0e27',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 240, 255, 0.25)' },
            { offset: 0.5, color: 'rgba(0, 240, 255, 0.08)' },
            { offset: 1, color: 'rgba(0, 240, 255, 0)' },
          ]),
        },
      },
    ],
    animationDuration: 1000,
    animationEasing: 'cubicOut',
  }
  trendChart.setOption(option, true)
}

const initRankingChart = () => {
  if (!rankingChartRef.value) return
  rankingChart = echarts.init(rankingChartRef.value)

  const barColors = [
    colors.cyan, colors.blue, colors.purple, colors.green,
    colors.orange, colors.pink, colors.indigo, colors.teal,
    colors.lime, colors.red,
  ]

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: colors.bgTooltip,
      borderColor: colors.borderGlow,
      borderWidth: 1,
      textStyle: { color: colors.textPrimary, fontSize: 13 },
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-weight:600">${p.name}</div>
                <div>用电量：<span style="color:${colors.cyan};font-weight:700">${p.value}</span> kWh</div>`
      },
    },
    grid: {
      left: 10,
      right: 50,
      top: 10,
      bottom: 10,
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: colors.textSecondary, fontSize: 11 },
    },
    yAxis: {
      type: 'category',
      data: deviceRanking.map((d) => d.name).reverse(),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textPrimary,
        fontSize: 13,
        fontWeight: 500,
      },
    },
    series: [
      {
        type: 'bar',
        data: deviceRanking.map((d, i) => ({
          value: d.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: barColors[9 - i] },
              { offset: 1, color: barColors[9 - i] + '66' },
            ]),
            borderRadius: [0, 4, 4, 0],
          },
        })).reverse(),
        barWidth: 16,
        label: {
          show: true,
          position: 'right',
          color: colors.textSecondary,
          fontSize: 12,
          formatter: '{c} kWh',
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 12,
            shadowColor: 'rgba(0, 240, 255, 0.3)',
          },
        },
      },
    ],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
  }
  rankingChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)

  const pieColors = [
    colors.cyan, colors.blue, colors.purple,
    colors.green, colors.orange, colors.pink,
  ]

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: colors.bgTooltip,
      borderColor: colors.borderGlow,
      borderWidth: 1,
      textStyle: { color: colors.textPrimary, fontSize: 13 },
      formatter: (params) => {
        return `<div style="font-weight:600;margin-bottom:4px">${params.name}</div>
                <div>用电量：<span style="color:${colors.cyan};font-weight:700">${params.value}</span> kWh</div>
                <div>占比：<span style="font-weight:700">${params.percent}</span>%</div>`
      },
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: colors.textPrimary, fontSize: 13 },
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 16,
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: false,
        padAngle: 3,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#0a0e27',
          borderWidth: 2,
        },
        label: {
          show: false,
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: colors.textPrimary,
            formatter: '{b}\n{d}%',
          },
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(0, 240, 255, 0.4)',
          },
        },
        labelLine: { show: false },
        data: roomDistribution.map((d, i) => ({
          ...d,
          itemStyle: { color: pieColors[i] },
        })),
        animationType: 'scale',
        animationDuration: 1200,
        animationEasing: 'elasticOut',
      },
      // Center decoration ring
      {
        type: 'pie',
        radius: ['38%', '40%'],
        center: ['38%', '50%'],
        silent: true,
        label: { show: false },
        data: [{ value: 1, itemStyle: { color: 'rgba(0, 240, 255, 0.08)' } }],
        animation: false,
      },
    ],
  }
  pieChart.setOption(option)
}

// ===== Count-up Animation =====
let animFrame = null
const animateCount = (targetValues) => {
  const duration = 1500
  const startTime = performance.now()
  const startValues = [...animatedValues]

  const step = (now) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const ease = 1 - Math.pow(1 - progress, 3)

    for (let i = 0; i < 4; i++) {
      const val = startValues[i] + (targetValues[i] - startValues[i]) * ease
      animatedValues[i] = i === 3 ? parseFloat(val.toFixed(1)) : parseFloat(val.toFixed(1))
    }

    if (progress < 1) {
      animFrame = requestAnimationFrame(step)
    }
  }
  animFrame = requestAnimationFrame(step)
}

// ===== Resize Handler =====
const handleResize = () => {
  trendChart?.resize()
  rankingChart?.resize()
  pieChart?.resize()
}

// ===== Lifecycle =====
onMounted(async () => {
  await nextTick()

  // Start count-up
  animateCount(statCards.map((c) => c.value))

  // Init charts
  initTrendChart()
  initRankingChart()
  initPieChart()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (animFrame) cancelAnimationFrame(animFrame)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  rankingChart?.dispose()
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
/* ===== Variables ===== */
$bg-primary: #0a0e27;
$bg-card: rgba(15, 21, 53, 0.85);
$border-glow: rgba(0, 240, 255, 0.15);
$cyan: #00f0ff;
$blue: #3b82f6;
$purple: #8b5cf6;
$green: #22c55e;
$orange: #f59e0b;
$red: #ef4444;
$text-primary: #e2e8f0;
$text-secondary: #94a3b8;
$text-dim: #475569;
$radius: 14px;

/* ===== Keyframes ===== */
@keyframes float-orb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.05); }
  66% { transform: translate(-20px, 15px) scale(0.95); }
}

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes gridScroll {
  from { background-position: 0 0; }
  to { background-position: 40px 40px; }
}

@keyframes iconBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* ===== Energy Root ===== */
.energy {
  position: relative;
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: $bg-primary;
  color: $text-primary;
  overflow: hidden;
}

/* ===== Grid Background ===== */
.grid-bg {
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(rgba($cyan, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba($cyan, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  animation: gridScroll 20s linear infinite;
  pointer-events: none;
  z-index: 0;
}

/* ===== Floating Glow Orbs ===== */
.glow-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
  animation: float-orb 12s ease-in-out infinite;
}
.glow-orb-1 {
  width: 350px; height: 350px;
  top: -80px; left: -80px;
  background: radial-gradient(circle, rgba($cyan, 0.2), transparent 70%);
}
.glow-orb-2 {
  width: 300px; height: 300px;
  bottom: -60px; right: -60px;
  background: radial-gradient(circle, rgba($purple, 0.18), transparent 70%);
  animation-delay: -4s;
}

/* ===== Stat Cards Row ===== */
.stat-row {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: $radius;
  background: $bg-card;
  border: 1px solid $border-glow;
  backdrop-filter: blur(12px);
  overflow: hidden;
  animation: fadeSlideUp 0.6s ease both;
  transition: transform 0.3s, box-shadow 0.3s;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.08s; }
  &:nth-child(3) { animation-delay: 0.16s; }
  &:nth-child(4) { animation-delay: 0.24s; }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }

  &__glow {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    border-radius: $radius $radius 0 0;
  }

  &--cyan {
    border-color: rgba($cyan, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $cyan, transparent); }
    .stat-card__icon { background: rgba($cyan, 0.15); color: $cyan; }
    &:hover { box-shadow: 0 8px 32px rgba($cyan, 0.15); }
  }
  &--blue {
    border-color: rgba($blue, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $blue, transparent); }
    .stat-card__icon { background: rgba($blue, 0.15); color: $blue; }
    &:hover { box-shadow: 0 8px 32px rgba($blue, 0.15); }
  }
  &--green {
    border-color: rgba($green, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $green, transparent); }
    .stat-card__icon { background: rgba($green, 0.15); color: $green; }
    &:hover { box-shadow: 0 8px 32px rgba($green, 0.15); }
  }
  &--orange {
    border-color: rgba($orange, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $orange, transparent); }
    .stat-card__icon { background: rgba($orange, 0.15); color: $orange; }
    &:hover { box-shadow: 0 8px 32px rgba($orange, 0.15); }
  }

  &__icon {
    width: 48px; height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    flex-shrink: 0;
    animation: iconBounce 3s ease-in-out infinite;
    .stat-card:nth-child(2) & { animation-delay: -0.5s; }
    .stat-card:nth-child(3) & { animation-delay: -1s; }
    .stat-card:nth-child(4) & { animation-delay: -1.5s; }
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__value {
    font-size: 26px;
    font-weight: 700;
    letter-spacing: -0.5px;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
  }

  &__unit {
    font-size: 13px;
    font-weight: 400;
    color: $text-secondary;
    margin-left: 4px;
  }

  &__label {
    font-size: 13px;
    color: $text-secondary;
    margin-top: 4px;
  }

  &__trend {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    font-weight: 600;
    padding: 3px 8px;
    border-radius: 20px;
    flex-shrink: 0;
  }
}

.trend-up {
  color: $red;
  background: rgba($red, 0.1);
}

.trend-down {
  color: $green;
  background: rgba($green, 0.1);
}

/* ===== Chart Row ===== */
.chart-row {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
  margin-bottom: 24px;
  animation: fadeSlideUp 0.6s ease 0.3s both;

  &--single {
    grid-template-columns: 1fr;
    animation-delay: 0.4s;
  }
}

/* ===== Panel Shared ===== */
.panel {
  background: $bg-card;
  border: 1px solid $border-glow;
  border-radius: $radius;
  backdrop-filter: blur(12px);
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 22px;
    border-bottom: 1px solid rgba($cyan, 0.08);
    position: relative;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
  }

  &__title-icon {
    width: 30px; height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: rgba($cyan, 0.12);
    color: $cyan;
    font-size: 15px;

    &--orange {
      background: rgba($orange, 0.12);
      color: $orange;
    }
    &--purple {
      background: rgba($purple, 0.12);
      color: $purple;
    }
  }

  &__body {
    padding: 20px 22px;
  }
}

/* ===== Trend Switch ===== */
.trend-switch {
  :deep(.el-radio-button__inner) {
    background: transparent;
    border-color: rgba($cyan, 0.2);
    color: $text-secondary;
    padding: 5px 14px;
    font-size: 12px;

    &:hover {
      color: $cyan;
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: rgba($cyan, 0.15);
    border-color: $cyan;
    color: $cyan;
    box-shadow: -1px 0 0 0 rgba($cyan, 0.3);
  }
}

/* ===== Chart Container ===== */
.chart-container {
  width: 100%;
  height: 340px;

  &--pie {
    height: 380px;
  }
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stat-row { grid-template-columns: 1fr; }
  .energy { padding: 12px; }
  .chart-container { height: 280px; }
  .chart-container--pie { height: 320px; }
}
</style>
