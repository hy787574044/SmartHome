<template>
  <div class="page-container">
    <div class="page-header">
      <h2>关于系统</h2>
    </div>

    <!-- 系统概况 -->
    <el-card shadow="hover" class="about-card">
      <div class="system-banner">
        <div class="logo-large">
          <svg viewBox="0 0 80 80" fill="none">
            <defs>
              <linearGradient id="aboutGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#00d4ff" />
                <stop offset="100%" stop-color="#7b61ff" />
              </linearGradient>
            </defs>
            <path d="M40 8L12 28v32l28 20 28-20V28L40 8z" stroke="url(#aboutGrad)" stroke-width="3" fill="none" />
            <circle cx="40" cy="40" r="12" fill="url(#aboutGrad)" />
          </svg>
        </div>
        <div class="system-title">
          <h1>SmartHome</h1>
          <p class="subtitle">全屋智能控制系统</p>
          <el-tag type="primary" size="large">v{{ version }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 系统信息 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>系统信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">SmartHome 全屋智能控制系统</el-descriptions-item>
        <el-descriptions-item label="版本号">v{{ version }}</el-descriptions-item>
        <el-descriptions-item label="运行时间">{{ systemInfo.uptime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备总数">{{ systemInfo.totalDevices ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="在线设备">{{ systemInfo.onlineDevices ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户总数">{{ systemInfo.totalUsers ?? '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 技术栈 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <el-icon><Cpu /></el-icon>
          <span>技术栈</span>
        </div>
      </template>
      <div class="tech-grid">
        <div class="tech-item" v-for="tech in techStack" :key="tech.name">
          <div class="tech-icon" :style="{ background: tech.color }">
            {{ tech.icon }}
          </div>
          <div class="tech-info">
            <div class="tech-name">{{ tech.name }}</div>
            <div class="tech-desc">{{ tech.desc }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 开源信息 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Link /></el-icon>
          <span>开源信息</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="开源协议">
          <el-tag type="success">MIT License</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="GitHub仓库">
          <el-link type="primary" href="https://github.com/smarthome/smarthome" target="_blank">
            https://github.com/smarthome/smarthome
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="问题反馈">
          <el-link type="primary" href="https://github.com/smarthome/smarthome/issues" target="_blank">
            提交Issue
          </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSystemInfo } from '@/api/system'

const version = ref('1.0.0')
const systemInfo = ref({})

const techStack = [
  { name: 'Spring Boot', desc: '后端框架', icon: 'S', color: '#6db33b' },
  { name: 'Vue 3', desc: '前端框架', icon: 'V', color: '#42b883' },
  { name: 'Element Plus', desc: 'UI组件库', icon: 'E', color: '#409eff' },
  { name: 'MyBatis-Plus', desc: 'ORM框架', icon: 'M', color: '#e3514a' },
  { name: 'MySQL', desc: '关系数据库', icon: 'D', color: '#00758f' },
  { name: 'Redis', desc: '缓存数据库', icon: 'R', color: '#dc382d' },
  { name: 'MQTT', desc: '物联网协议', icon: 'Q', color: '#660066' },
  { name: 'ECharts', desc: '数据可视化', icon: 'C', color: '#aa3578' },
]

onMounted(async () => {
  try {
    const res = await getSystemInfo()
    systemInfo.value = res.data || {}
    if (res.data?.version) {
      version.value = res.data.version
    }
  } catch {
    // ignore
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: #f0f4f8;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #f0f4f8;

  .el-icon {
    color: #00d4ff;
  }
}

.about-card {
  margin-bottom: 16px;
}

.system-banner {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 24px 0;
}

.logo-large {
  width: 80px;
  height: 80px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
  }
}

.system-title {
  h1 {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(90deg, #00d4ff, #7b61ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 16px;
    color: #94a3b8;
    margin: 0 0 12px 0;
  }
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.tech-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 212, 255, 0.08);
    transform: translateY(-2px);
  }
}

.tech-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}

.tech-info {
  .tech-name {
    font-size: 15px;
    font-weight: 600;
    color: #f0f4f8;
  }

  .tech-desc {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 2px;
  }
}
</style>
