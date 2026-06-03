<template>
  <div class="layout-wrapper">
    <ParticleBackground />
    <el-container class="layout-container">
      <!-- 移动端侧边栏抽屉遮罩 -->
      <div
        v-if="isMobile && mobileDrawerVisible"
        class="mobile-drawer-overlay"
        @click="mobileDrawerVisible = false"
      ></div>

      <!-- 侧边栏（桌面端） -->
      <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '220px'" class="aside">
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
          :unique-opened="true"
          router
          class="side-menu"
          @select="handleMenuClick"
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

          <el-sub-menu index="scene">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>场景联动</span>
            </template>
            <el-menu-item index="/scene">场景列表</el-menu-item>
            <el-menu-item index="/scene/templates">场景模板</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="alert">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>告警监控</span>
            </template>
            <el-menu-item index="/alert/rules">告警规则</el-menu-item>
            <el-menu-item index="/alert/logs">告警日志</el-menu-item>
            <el-menu-item index="/notification">通知设置</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="analytics">
            <template #title>
              <el-icon><DataLine /></el-icon>
              <span>数据分析</span>
            </template>
            <el-menu-item index="/analytics/energy">能耗统计</el-menu-item>
            <el-menu-item index="/analytics/alert">告警统计</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/family">
            <el-icon><Van /></el-icon>
            <template #title>家庭管理</template>
          </el-menu-item>

          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/log">操作日志</el-menu-item>
            <el-menu-item index="/system/settings">系统设置</el-menu-item>
            <el-menu-item index="/system/about">关于系统</el-menu-item>
          </el-sub-menu>
        </el-menu>

        <div class="aside-deco-line"></div>
      </el-aside>

      <!-- 侧边栏（移动端抽屉） -->
      <transition name="slide-drawer">
        <el-aside
          v-if="isMobile && mobileDrawerVisible"
          width="260px"
          class="aside mobile-drawer"
        >
          <div class="logo" @click="$router.push('/')">
            <div class="logo-icon-wrapper">
              <div class="logo-ring"></div>
              <svg class="logo-svg" viewBox="0 0 40 40" fill="none">
                <defs>
                  <linearGradient id="logoGradMobile" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#00d4ff" />
                    <stop offset="100%" stop-color="#7b61ff" />
                  </linearGradient>
                </defs>
                <path d="M20 4L6 14v16l14 10 14-10V14L20 4z" stroke="url(#logoGradMobile)" stroke-width="2" fill="none" />
                <circle cx="20" cy="20" r="6" fill="url(#logoGradMobile)" />
              </svg>
            </div>
            <span class="logo-text">SmartHome</span>
          </div>

          <el-menu
            :default-active="activeMenu"
            :collapse="false"
            router
            class="side-menu"
            @select="handleMenuClick"
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

            <el-sub-menu index="scene">
              <template #title>
                <el-icon><Connection /></el-icon>
                <span>场景联动</span>
              </template>
              <el-menu-item index="/scene">场景列表</el-menu-item>
              <el-menu-item index="/scene/templates">场景模板</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="alert">
              <template #title>
                <el-icon><Bell /></el-icon>
                <span>告警监控</span>
              </template>
              <el-menu-item index="/alert/rules">告警规则</el-menu-item>
              <el-menu-item index="/alert/logs">告警日志</el-menu-item>
              <el-menu-item index="/notification">通知设置</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="analytics">
              <template #title>
                <el-icon><DataLine /></el-icon>
                <span>数据分析</span>
              </template>
              <el-menu-item index="/analytics/energy">能耗统计</el-menu-item>
              <el-menu-item index="/analytics/alert">告警统计</el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/family">
              <el-icon><Van /></el-icon>
              <template #title>家庭管理</template>
            </el-menu-item>

            <el-sub-menu index="system">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/log">操作日志</el-menu-item>
              <el-menu-item index="/system/settings">系统设置</el-menu-item>
              <el-menu-item index="/system/about">关于系统</el-menu-item>
            </el-sub-menu>
          </el-menu>

          <div class="aside-deco-line"></div>
        </el-aside>
      </transition>

      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon v-if="isMobile" class="collapse-btn" @click="mobileDrawerVisible = !mobileDrawerVisible">
              <Operation />
            </el-icon>
            <el-icon v-else class="collapse-btn" @click="isCollapse = !isCollapse">
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
                <span v-show="!isCollapse || isMobile" class="user-name">{{ userName }}</span>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { connectWebSocket, disconnectWebSocket, onWebSocketMessage, offWebSocketMessage } from '@/utils/websocket'
import { ElNotification, ElMessageBox } from 'element-plus'
import { Operation } from '@element-plus/icons-vue'
import ParticleBackground from '@/components/ParticleBackground.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const alertCount = ref(0)
const userName = ref('Admin')

// 移动端响应式检测
const isMobile = ref(window.innerWidth < 768)
const mobileDrawerVisible = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

// 移动端默认收起侧边栏
watch(isMobile, (val) => {
  if (val) {
    isCollapse.value = true
    mobileDrawerVisible.value = false
  }
})

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

// 移动端菜单点击后自动关闭侧边栏
const handleMenuClick = () => {
  if (isMobile.value) {
    mobileDrawerVisible.value = false
  }
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
  window.addEventListener('resize', handleResize)
  // 初始化移动端状态
  if (isMobile.value) isCollapse.value = true
})

onUnmounted(() => {
  offWebSocketMessage('deviceStatus', handleDeviceStatus)
  offWebSocketMessage('alert', handleAlert)
  disconnectWebSocket()
  window.removeEventListener('resize', handleResize)
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

// 移动端抽屉遮罩
.mobile-drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

// 移动端抽屉侧边栏
.mobile-drawer {
  position: fixed !important;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1000;
}

// 抽屉滑入/滑出动画
.slide-drawer-enter-active,
.slide-drawer-leave-active {
  transition: transform 0.3s ease;
}

.slide-drawer-enter-from,
.slide-drawer-leave-to {
  transform: translateX(-100%);
}

// 媒体查询 - 移动端适配
@media (max-width: 768px) {
  .aside {
    position: fixed;
    z-index: 1000;
  }

  .header {
    padding: 0 12px;
  }

  .main {
    padding: 12px;
  }

  // 移动端表格横向滚动
  :deep(.el-table) {
    width: 100% !important;
    overflow-x: auto;

    .el-table__body-wrapper {
      overflow-x: auto;
    }
  }

  :deep(.el-table__body),
  :deep(.el-table__header) {
    min-width: 600px;
  }

  // 移动端对话框全屏显示
  :deep(.el-dialog) {
    width: 100% !important;
    margin: 0 !important;
    border-radius: 0 !important;
    max-height: 100vh;
    height: 100vh;
    display: flex;
    flex-direction: column;

    .el-dialog__header {
      flex-shrink: 0;
    }

    .el-dialog__body {
      flex: 1;
      overflow-y: auto;
    }
  }

  :deep(.el-overlay) {
    z-index: 2000;
  }
}
</style>
