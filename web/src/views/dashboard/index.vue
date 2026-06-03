<template>
  <div class="dashboard">
    <!-- Decorative grid background -->
    <div class="grid-bg"></div>
    <div class="glow-orb glow-orb-1"></div>
    <div class="glow-orb glow-orb-2"></div>
    <div class="glow-orb glow-orb-3"></div>

    <!-- Stat Cards -->
    <div class="stat-row">
      <div
        v-for="(card, idx) in statCards"
        :key="idx"
        class="stat-card"
        :class="`stat-card--${card.theme}`"
      >
        <div class="stat-card__glow"></div>
        <div class="stat-card__icon">
          <el-icon :size="32"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">
            <span class="count-num">{{ animatedValues[idx] }}</span>
          </div>
          <div class="stat-card__label">{{ card.label }}</div>
        </div>
        <div class="stat-card__spark">
          <svg viewBox="0 0 120 40" preserveAspectRatio="none">
            <polyline :points="card.sparkline" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
      </div>
    </div>

    <!-- Main Content Row -->
    <div class="main-row">
      <!-- Device Overview -->
      <div class="panel panel--devices">
        <div class="panel__header">
          <div class="panel__title">
            <span class="panel__title-icon">
              <el-icon><Monitor /></el-icon>
            </span>
            设备概览
          </div>
          <div class="panel__header-decor"></div>
        </div>
        <div class="panel__body">
          <div v-if="rooms.length === 0" class="empty-state">
            <el-empty description="暂无房间，请先添加房间" />
          </div>
          <div v-else class="rooms-container">
            <div v-for="room in rooms" :key="room.roomId" class="room-block">
              <div class="room-block__header">
                <span class="room-block__name">{{ room.roomName }}</span>
                <span class="room-block__count">{{ getDevicesByRoom(room.roomId).length }} 台设备</span>
              </div>
              <div class="device-grid">
                <div
                  v-for="device in getDevicesByRoom(room.roomId)"
                  :key="device.deviceId"
                  class="device-card"
                  :class="{ 'device-card--online': device.status === 3, 'device-card--offline': device.status !== 3 }"
                >
                  <div class="device-card__status-dot" :class="{ 'is-online': device.status === 3 }"></div>
                  <div class="device-card__icon">
                    <el-icon :size="22"><Monitor /></el-icon>
                  </div>
                  <div class="device-card__info">
                    <div class="device-card__name">{{ device.deviceName }}</div>
                    <div class="device-card__status-text">
                      {{ device.status === 3 ? '在线' : '离线' }}
                    </div>
                  </div>
                  <div v-if="device.properties" class="device-card__props">
                    <div
                      v-for="(value, key) in device.properties"
                      :key="key"
                      class="device-card__prop"
                    >
                      <span class="device-card__prop-key">{{ key }}</span>
                      <span class="device-card__prop-val">{{ value }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Alerts Timeline -->
      <div class="panel panel--alerts">
        <div class="panel__header">
          <div class="panel__title">
            <span class="panel__title-icon panel__title-icon--warn">
              <el-icon><Warning /></el-icon>
            </span>
            最近告警
          </div>
          <el-button class="view-all-btn" text @click="$router.push('/alert/logs')">
            查看全部
            <el-icon class="view-all-btn__arrow"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="panel__body">
          <div v-if="recentAlerts.length === 0" class="empty-state">
            <el-empty description="暂无告警" :image-size="60" />
          </div>
          <div v-else class="timeline">
            <div
              v-for="(alert, idx) in recentAlerts"
              :key="alert.logId"
              class="timeline-item"
              :style="{ animationDelay: `${idx * 0.1}s` }"
            >
              <div class="timeline-item__line"></div>
              <div
                class="timeline-item__dot"
                :class="`timeline-item__dot--${getAlertTheme(alert.alertLevel)}`"
              ></div>
              <div class="timeline-item__card">
                <div class="timeline-item__level">
                  <span
                    class="level-badge"
                    :class="`level-badge--${getAlertTheme(alert.alertLevel)}`"
                  >
                    {{ getAlertLevelText(alert.alertLevel) }}
                  </span>
                </div>
                <div class="timeline-item__msg">{{ alert.alertMessage }}</div>
                <div class="timeline-item__time">
                  <el-icon :size="12"><Clock /></el-icon>
                  {{ alert.createTime }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import { listRooms } from '@/api/room'
import { listDevices } from '@/api/device'
import { listAlertLogs } from '@/api/alert'
import {
  Monitor, Warning, ArrowRight, Clock,
  Cpu, Connection, CircleClose, Bell
} from '@element-plus/icons-vue'

const stats = ref({ totalDevices: 0, onlineDevices: 0, offlineDevices: 0 })
const alertCount = ref(0)
const rooms = ref([])
const devices = ref([])
const recentAlerts = ref([])

const animatedValues = reactive([0, 0, 0, 0])

const statCards = [
  { key: 'totalDevices', label: '设备总数', icon: Cpu, theme: 'blue', sparkline: '0,35 10,28 20,32 30,20 40,25 50,15 60,22 70,10 80,18 90,8 100,12 110,5 120,10' },
  { key: 'onlineDevices', label: '在线设备', icon: Connection, theme: 'green', sparkline: '0,30 10,25 20,28 30,18 40,22 50,12 60,20 70,8 80,15 90,5 100,10 110,3 120,8' },
  { key: 'offlineDevices', label: '离线设备', icon: CircleClose, theme: 'gray', sparkline: '0,10 10,15 20,12 30,20 40,18 50,25 60,15 70,22 80,18 90,28 100,20 110,25 120,18' },
  { key: 'alertCount', label: '待处理告警', icon: Bell, theme: 'orange', sparkline: '0,20 10,15 20,25 30,18 40,30 50,22 60,35 70,28 80,32 90,20 100,25 110,15 120,22' },
]

const getDevicesByRoom = (roomId) => {
  return devices.value.filter((d) => d.roomId === roomId)
}

const getAlertTheme = (level) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] || 'info'
}

const getAlertLevelText = (level) => {
  const map = { 1: '提示', 2: '警告', 3: '严重' }
  return map[level] || '提示'
}

// Count-up animation
let animFrame = null
const animateCount = (targetValues) => {
  const duration = 1500
  const startTime = performance.now()
  const startValues = [...animatedValues]

  const step = (now) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    // Ease-out cubic
    const ease = 1 - Math.pow(1 - progress, 3)

    for (let i = 0; i < 4; i++) {
      animatedValues[i] = Math.round(startValues[i] + (targetValues[i] - startValues[i]) * ease)
    }

    if (progress < 1) {
      animFrame = requestAnimationFrame(step)
    }
  }
  animFrame = requestAnimationFrame(step)
}

onMounted(async () => {
  try {
    const [statsRes, roomsRes, devicesRes, alertsRes] = await Promise.all([
      getDashboardStats(),
      listRooms(),
      listDevices({ pageSize: 100 }),
      listAlertLogs({ status: 1, pageNum: 1, pageSize: 5 }),
    ])
    stats.value = statsRes.data
    rooms.value = roomsRes.data
    devices.value = devicesRes.data.rows
    recentAlerts.value = alertsRes.data.rows
    alertCount.value = alertsRes.data.total

    // Trigger count-up animation
    animateCount([
      stats.value.totalDevices,
      stats.value.onlineDevices,
      stats.value.offlineDevices,
      alertCount.value,
    ])
  } catch (e) {
    console.error('加载数据失败:', e)
  }
})

onBeforeUnmount(() => {
  if (animFrame) cancelAnimationFrame(animFrame)
})
</script>

<style lang="scss" scoped>
/* ===== Variables ===== */
$bg-primary: #0a0e27;
$bg-secondary: #0f1535;
$bg-card: rgba(15, 21, 53, 0.85);
$border-glow: rgba(0, 240, 255, 0.15);
$cyan: #00f0ff;
$blue: #3b82f6;
$purple: #8b5cf6;
$green: #22c55e;
$orange: #f59e0b;
$red: #ef4444;
$gray: #64748b;
$text-primary: #e2e8f0;
$text-secondary: #94a3b8;
$text-dim: #475569;
$radius: 14px;

/* ===== Keyframes ===== */
@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 0 0 rgba($green, 0.6); }
  50% { box-shadow: 0 0 0 6px rgba($green, 0); }
}

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

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes iconBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* ===== Dashboard Root ===== */
.dashboard {
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
  width: 400px; height: 400px;
  top: -100px; left: -100px;
  background: radial-gradient(circle, rgba($blue, 0.25), transparent 70%);
}
.glow-orb-2 {
  width: 350px; height: 350px;
  bottom: -50px; right: -50px;
  background: radial-gradient(circle, rgba($purple, 0.2), transparent 70%);
  animation-delay: -4s;
}
.glow-orb-3 {
  width: 300px; height: 300px;
  top: 50%; left: 50%;
  background: radial-gradient(circle, rgba($cyan, 0.12), transparent 70%);
  animation-delay: -8s;
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
  gap: 16px;
  padding: 22px 20px;
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

  &--blue {
    border-color: rgba($blue, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $blue, transparent); }
    .stat-card__icon { background: rgba($blue, 0.15); color: $blue; }
    .stat-card__spark { color: $blue; }
    &:hover { box-shadow: 0 8px 32px rgba($blue, 0.15); }
  }
  &--green {
    border-color: rgba($green, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $green, transparent); }
    .stat-card__icon { background: rgba($green, 0.15); color: $green; }
    .stat-card__spark { color: $green; }
    &:hover { box-shadow: 0 8px 32px rgba($green, 0.15); }
  }
  &--gray {
    border-color: rgba($gray, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $gray, transparent); }
    .stat-card__icon { background: rgba($gray, 0.15); color: $gray; }
    .stat-card__spark { color: $gray; }
    &:hover { box-shadow: 0 8px 32px rgba($gray, 0.15); }
  }
  &--orange {
    border-color: rgba($orange, 0.25);
    .stat-card__glow { background: linear-gradient(90deg, transparent, $orange, transparent); }
    .stat-card__icon { background: rgba($orange, 0.15); color: $orange; }
    .stat-card__spark { color: $orange; }
    &:hover { box-shadow: 0 8px 32px rgba($orange, 0.15); }
  }

  &__icon {
    width: 52px; height: 52px;
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
    font-size: 30px;
    font-weight: 700;
    letter-spacing: -0.5px;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
  }

  &__label {
    font-size: 13px;
    color: $text-secondary;
    margin-top: 4px;
  }

  &__spark {
    position: absolute;
    bottom: 8px; right: 12px;
    width: 100px; height: 36px;
    opacity: 0.3;
    svg { width: 100%; height: 100%; }
  }
}

/* ===== Main Row ===== */
.main-row {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  animation: fadeSlideUp 0.6s ease 0.3s both;
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
    padding: 18px 22px;
    border-bottom: 1px solid rgba($cyan, 0.08);
    position: relative;
  }

  &__header-decor {
    position: absolute;
    bottom: 0; left: 22px;
    width: 60px; height: 2px;
    background: linear-gradient(90deg, $cyan, transparent);
    border-radius: 2px;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }

  &__title-icon {
    width: 32px; height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: rgba($cyan, 0.12);
    color: $cyan;
    font-size: 16px;

    &--warn {
      background: rgba($orange, 0.12);
      color: $orange;
    }
  }

  &__body {
    padding: 20px 22px;
    max-height: calc(100vh - 280px);
    overflow-y: auto;

    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-track { background: transparent; }
    &::-webkit-scrollbar-thumb { background: rgba($cyan, 0.2); border-radius: 4px; }
  }
}

/* ===== View All Button ===== */
.view-all-btn {
  color: $cyan !important;
  font-size: 13px;
  padding: 0 !important;
  &:hover { color: lighten($cyan, 10%) !important; }
  &__arrow { margin-left: 4px; transition: transform 0.2s; }
  &:hover .view-all-btn__arrow { transform: translateX(3px); }
}

/* ===== Device Overview ===== */
.rooms-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.room-block {
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba($cyan, 0.06);
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
    position: relative;
    padding-left: 14px;

    &::before {
      content: '';
      position: absolute;
      left: 0; top: 50%;
      transform: translateY(-50%);
      width: 4px; height: 16px;
      border-radius: 2px;
      background: linear-gradient(180deg, $cyan, $blue);
    }
  }

  &__count {
    font-size: 12px;
    color: $text-dim;
    background: rgba($cyan, 0.06);
    padding: 2px 10px;
    border-radius: 20px;
  }
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.device-card {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    background: rgba($cyan, 0.04);
    border-color: rgba($cyan, 0.15);
    transform: translateY(-2px);
  }

  &--online {
    border-color: rgba($green, 0.15);
    &:hover { border-color: rgba($green, 0.3); }
  }

  &--offline {
    opacity: 0.6;
  }

  /* Status dot */
  &__status-dot {
    position: absolute;
    top: 10px; right: 10px;
    width: 8px; height: 8px;
    border-radius: 50%;
    background: $gray;

    &.is-online {
      background: $green;
      animation: pulse-green 2s ease-in-out infinite;
    }
  }

  &__icon {
    width: 40px; height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: rgba($cyan, 0.08);
    color: $cyan;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: 13px;
    font-weight: 600;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__status-text {
    font-size: 11px;
    color: $text-dim;
    margin-top: 2px;
  }

  &__props {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 6px 12px;
    padding-top: 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.04);
  }

  &__prop {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;

    &-key {
      color: $text-dim;
    }
    &-val {
      color: $cyan;
      font-weight: 600;
    }
  }
}

/* ===== Alert Timeline ===== */
.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
  animation: fadeSlideUp 0.4s ease both;

  &:last-child {
    padding-bottom: 0;
  }

  &__line {
    position: absolute;
    left: -24px;
    top: 10px;
    bottom: 0;
    width: 2px;
    background: linear-gradient(180deg, rgba($cyan, 0.3), rgba($cyan, 0.05));
  }

  &:last-child .timeline-item__line {
    background: linear-gradient(180deg, rgba($cyan, 0.3), transparent);
  }

  &__dot {
    position: absolute;
    left: -28px;
    top: 6px;
    width: 10px; height: 10px;
    border-radius: 50%;
    border: 2px solid;
    background: $bg-primary;

    &--info {
      border-color: $blue;
      box-shadow: 0 0 8px rgba($blue, 0.4);
    }
    &--warning {
      border-color: $orange;
      box-shadow: 0 0 8px rgba($orange, 0.4);
    }
    &--danger {
      border-color: $red;
      box-shadow: 0 0 8px rgba($red, 0.4);
      animation: borderGlow 2s ease-in-out infinite;
    }
  }

  &__card {
    padding: 12px 16px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.02);
    border: 1px solid rgba(255, 255, 255, 0.04);
    transition: background 0.3s;

    &:hover {
      background: rgba($cyan, 0.04);
    }
  }

  &__level {
    margin-bottom: 6px;
  }

  &__msg {
    font-size: 13px;
    color: $text-primary;
    line-height: 1.5;
    margin-bottom: 6px;
  }

  &__time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: $text-dim;
  }
}

/* ===== Level Badge ===== */
.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;

  &--info {
    background: rgba($blue, 0.12);
    color: $blue;
    border: 1px solid rgba($blue, 0.2);
  }
  &--warning {
    background: rgba($orange, 0.12);
    color: $orange;
    border: 1px solid rgba($orange, 0.2);
  }
  &--danger {
    background: rgba($red, 0.12);
    color: $red;
    border: 1px solid rgba($red, 0.2);
  }
}

/* ===== Empty State ===== */
.empty-state {
  padding: 20px 0;
  :deep(.el-empty__description p) {
    color: $text-dim;
  }
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
  .main-row { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stat-row { grid-template-columns: 1fr; }
  .dashboard { padding: 12px; }
}
</style>
