<template>
  <div class="layout-wrapper">
    <ParticleBackground />
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
        <div class="logo" @click="$router.push('/')">
          <div class="logo-icon-wrapper">
            <div class="logo-ring"></div>
            <svg class="logo-svg" viewBox="0 0 40 40" fill="none">
              <defs>
                <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#00d4ff" />
                  <stop offset="100%" stop-color="#7b61ff" />
                </linearGradient>
              </defs>
              <path d="M20 4L6 14v16l14 10 14-10V14L20 4z" stroke="url(#logoGrad)" stroke-width="2" fill="none" />
              <circle cx="20" cy="20" r="6" fill="url(#logoGrad)" />
            </svg>
          </div>
          <transition name="fade-text">
            <span v-show="!isCollapse" class="logo-text">SmartHome</span>
          </transition>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          class="side-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="device">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </template>
            <el-menu-item index="/device/list">设备列表</el-menu-item>
            <el-menu-item index="/device/product">产品管理</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/room">
            <el-icon><House /></el-icon>
            <template #title>房间管理</template>
          </el-menu-item>

          <el-menu-item index="/scene">
            <el-icon><Connection /></el-icon>
            <template #title>场景联动</template>
          </el-menu-item>

          <el-sub-menu index="alert">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>告警监控</span>
            </template>
            <el-menu-item index="/alert/rules">告警规则</el-menu-item>
            <el-menu-item index="/alert/logs">告警日志</el-menu-item>
          </el-sub-menu>
        </el-menu>

        <div class="aside-deco-line"></div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item">{{ item }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-badge :value="alertCount" :hidden="alertCount === 0" class="alert-badge">
              <el-icon size="20" @click="$router.push('/alert/logs')"><Bell /></el-icon>
            </el-badge>
            <el-dropdown @command="handleUserCommand" class="user-dropdown">
              <div class="user-info">
                <div class="user-avatar">
                  <el-icon><User /></el-icon>
                </div>
                <span v-show="!isCollapse" class="user-name">{{ userName }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <div class="dropdown-divider"></div>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { connectWebSocket, disconnectWebSocket, onWebSocketMessage, offWebSocketMessage } from '@/utils/websocket'
import { ElNotification, ElMessageBox } from 'element-plus'
import ParticleBackground from '@/components/ParticleBackground.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const alertCount = ref(0)
const userName = ref('Admin')

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const items = ['首页']
  if (route.meta?.parent) items.push(route.meta.parent)
  if (route.meta?.title && route.meta.title !== '首页') items.push(route.meta.title)
  return items
})

const handleDeviceStatus = (data) => console.log('设备状态更新:', data)

const handleAlert = (data) => {
  alertCount.value++
  ElNotification({
    title: '告警通知',
    message: data.alertMessage || '收到新的告警',
    type: 'warning',
    duration: 5000,
  })
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      localStorage.removeItem('token')
      router.push('/login')
    }).catch(() => {})
  }
}

onMounted(() => {
  connectWebSocket()
  onWebSocketMessage('deviceStatus', handleDeviceStatus)
  onWebSocketMessage('alert', handleAlert)
  const stored = localStorage.getItem('userName')
  if (stored) userName.value = stored
})

onUnmounted(() => {
  offWebSocketMessage('deviceStatus', handleDeviceStatus)
  offWebSocketMessage('alert', handleAlert)
  disconnectWebSocket()
})
</script>

<style lang="scss" scoped>
.layout-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.layout-container {
  position: relative;
  z-index: 10;
  height: 100vh;
  overflow: hidden;
}

// 侧边栏
.aside {
  background: #283045 !important;
  border-right: none;
  transition: width 0.3s;
  overflow: hidden;
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;

  // 右侧渐变边线
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 1px;
    height: 100%;
    background: linear-gradient(
      180deg,
      transparent 0%,
      rgba(0, 212, 255, 0.3) 30%,
      rgba(123, 97, 255, 0.2) 70%,
      transparent 100%
    );
  }
}

.side-menu {
  flex: 1;
  background: transparent !important;
  border-right: none !important;
  padding: 12px 10px;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: #b0bcc8;
    height: 50px;
    line-height: 50px;
    margin: 3px 0;
    border-radius: 10px;
    transition: all 0.3s;
    font-size: 15px;
    letter-spacing: 0.5px;

    &:hover {
      color: #fff;
      background: rgba(0, 212, 255, 0.1) !important;
    }

    .el-icon {
      font-size: 20px;
      margin-right: 10px;
    }
  }

  :deep(.el-menu-item.is-active) {
    color: #fff !important;
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(123, 97, 255, 0.12)) !important;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      top: 12px;
      bottom: 12px;
      width: 4px;
      border-radius: 0 4px 4px 0;
      background: linear-gradient(180deg, #00d4ff, #7b61ff);
      box-shadow: 0 0 8px rgba(0, 212, 255, 0.4);
    }

    .el-icon {
      color: #00d4ff;
    }
  }

  :deep(.el-sub-menu) {
    margin: 2px 0;
  }

  :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
    color: #f0f4f8;
    background: rgba(0, 212, 255, 0.05) !important;
    border-radius: 10px;
  }

  :deep(.el-menu--inline) {
    background: transparent !important;
    padding: 4px 0;

    .el-menu-item {
      padding-left: 56px !important;
      height: 44px;
      line-height: 44px;
      font-size: 14px;
      color: #b0bcc8;
      border-radius: 8px;
      margin: 2px 0;

      &:hover {
        color: #fff;
        background: rgba(0, 212, 255, 0.08) !important;
      }

      &.is-active {
        color: #00d4ff !important;
        background: rgba(0, 212, 255, 0.1) !important;
      }
    }
  }
}

// Logo
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(0, 212, 255, 0.08);
  flex-shrink: 0;
  padding: 0 16px;
}

.logo-icon-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-ring {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  border: 1.5px solid transparent;
  border-top-color: #00d4ff;
  border-right-color: #7b61ff;
  animation: logoSpin 6s linear infinite;
}

@keyframes logoSpin {
  to { transform: rotate(360deg); }
}

.logo-svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  background: linear-gradient(90deg, #00d4ff, #7b61ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.fade-text-enter-active,
.fade-text-leave-active {
  transition: opacity 0.3s;
}

.fade-text-enter-from,
.fade-text-leave-to {
  opacity: 0;
}

.aside-deco-line {
  height: 1px;
  margin: 0 16px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.2), rgba(123, 97, 255, 0.15), transparent);
}

// 主容器
.main-container {
  flex-direction: column;
}

// 顶部栏
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #283045 !important;
  border-bottom: none;
  padding: 0 24px;
  height: 60px;
  position: relative;

  // 底部渐变边线
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(
      90deg,
      rgba(0, 212, 255, 0.2),
      rgba(123, 97, 255, 0.15),
      rgba(0, 212, 255, 0.1)
    );
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      cursor: pointer;
      font-size: 20px;
      color: #b0bcc8;
      transition: color 0.3s;

      &:hover {
        color: #00d4ff;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .alert-badge {
      cursor: pointer;
      color: #b0bcc8;
      transition: color 0.3s;

      &:hover {
        color: #00d4ff;
      }
    }
  }
}

// 用户下拉
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px;
  border-radius: 10px;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 212, 255, 0.08);
  }
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(123, 97, 255, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00d4ff;
  font-size: 16px;
}

.user-name {
  color: #e8ecf1;
  font-size: 14px;
}

.arrow-icon {
  color: #a0aab8;
  font-size: 12px;
}

// 主内容区
.main {
  background: transparent !important;
  padding: 20px;
  overflow-y: auto;
  position: relative;
}

// 滚动条
.main::-webkit-scrollbar {
  width: 6px;
}

.main::-webkit-scrollbar-track {
  background: transparent;
}

.main::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}
</style>
