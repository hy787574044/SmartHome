<template>
  <div class="login-wrapper">
    <ParticleBackground />

    <!-- 主登录卡片 -->
    <div class="login-card" :class="{ 'card-enter': cardVisible }">
      <!-- 左侧品牌区域 -->
      <div class="brand-panel">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-ring"></div>
            <div class="logo-core">
              <svg viewBox="0 0 64 64" class="logo-svg">
                <defs>
                  <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#00d4ff" />
                    <stop offset="100%" stop-color="#7b61ff" />
                  </linearGradient>
                </defs>
                <rect x="12" y="28" width="40" height="28" rx="3" fill="none" stroke="url(#logoGrad)" stroke-width="2.5" />
                <polygon points="8,28 32,6 56,28" fill="none" stroke="url(#logoGrad)" stroke-width="2.5" stroke-linejoin="round" />
                <rect x="26" y="38" width="12" height="18" rx="1.5" fill="none" stroke="url(#logoGrad)" stroke-width="2" />
                <circle cx="32" cy="47" r="2" fill="url(#logoGrad)" />
              </svg>
            </div>
          </div>

          <h1 class="brand-title">SmartHome</h1>
          <p class="brand-subtitle">全屋智能控制系统</p>

          <div class="brand-features">
            <div class="feature-item" v-for="(item, i) in features" :key="i">
              <el-icon class="feature-icon"><component :is="item.icon" /></el-icon>
              <span>{{ item.text }}</span>
            </div>
          </div>
        </div>

        <!-- 装饰线条 -->
        <div class="deco-line deco-line-1"></div>
        <div class="deco-line deco-line-2"></div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-panel">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" class="remember-checkbox">记住我</el-checkbox>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>默认账号: admin / admin123</span>
        </div>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="bottom-deco">
      <div class="deco-glow"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor, Connection, DataLine } from '@element-plus/icons-vue'
import request from '@/api/request'
import ParticleBackground from '@/components/ParticleBackground.vue'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const cardVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const features = [
  { icon: 'Monitor', text: '设备集中管控' },
  { icon: 'Connection', text: '场景智能联动' },
  { icon: 'DataLine', text: '实时数据分析' },
]

const handleLogin = async () => {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
    })
    const token = res.data?.token || res.token
    if (token) {
      localStorage.setItem('token', token)
    }
    const userName = res.data?.nickname || res.data?.username || loginForm.username
    localStorage.setItem('userName', userName)
    if (rememberMe.value) {
      localStorage.setItem('rememberedUser', loginForm.username)
    } else {
      localStorage.removeItem('rememberedUser')
    }
    ElMessage.success('登录成功，欢迎回来!')
    router.push('/')
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const remembered = localStorage.getItem('rememberedUser')
  if (remembered) {
    loginForm.username = remembered
    rememberMe.value = true
  }
  setTimeout(() => { cardVisible.value = true }, 100)
})
</script>

<style lang="scss" scoped>
.login-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.login-card {
  position: relative;
  z-index: 10;
  display: flex;
  width: 900px;
  min-height: 520px;
  border-radius: 20px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  box-shadow:
    0 0 40px rgba(0, 212, 255, 0.1),
    0 0 80px rgba(123, 97, 255, 0.05),
    inset 0 0 40px rgba(0, 0, 0, 0.2);
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);

  &.card-enter {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 品牌区域
.brand-panel {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.05) 0%, rgba(123, 97, 255, 0.05) 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-logo {
  position: relative;
  width: 80px;
  height: 80px;
  margin-bottom: 30px;
}

.logo-ring {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: #00d4ff;
  border-right-color: #7b61ff;
  animation: logoSpin 4s linear infinite;
}

.logo-core {
  position: relative;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.15), rgba(123, 97, 255, 0.15));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: logoPulse 3s ease-in-out infinite;

  .logo-svg {
    width: 48px;
    height: 48px;
  }
}

@keyframes logoSpin {
  to { transform: rotate(360deg); }
}

@keyframes logoPulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 20px rgba(0, 212, 255, 0.2); }
  50% { transform: scale(1.05); box-shadow: 0 0 40px rgba(0, 212, 255, 0.3); }
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(90deg, #00d4ff, #7b61ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 16px;
  color: #94a3b8;
  margin-bottom: 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #cbd5e1;
  font-size: 14px;

  .feature-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.15), rgba(123, 97, 255, 0.15));
    border-radius: 10px;
    color: #00d4ff;
    font-size: 18px;
  }
}

// 装饰线条
.deco-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.3), transparent);

  &-1 {
    bottom: 30%;
    left: -10%;
    width: 120%;
    height: 1px;
    transform: rotate(-15deg);
    animation: lineFloat 6s ease-in-out infinite;
  }

  &-2 {
    top: 20%;
    left: -10%;
    width: 120%;
    height: 1px;
    transform: rotate(10deg);
    animation: lineFloat 8s ease-in-out infinite reverse;
  }
}

@keyframes lineFloat {
  0%, 100% { opacity: 0.3; transform: rotate(-15deg) translateY(0); }
  50% { opacity: 0.6; transform: rotate(-15deg) translateY(10px); }
}

// 表单区域
.form-panel {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 36px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
  }

  p {
    color: #64748b;
    font-size: 15px;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    border-radius: 12px;
    height: 48px;
    transition: all 0.3s;

    &:hover {
      border-color: rgba(0, 212, 255, 0.3) !important;
    }

    &.is-focus {
      border-color: #00d4ff !important;
      box-shadow: 0 0 20px rgba(0, 212, 255, 0.15) !important;
    }
  }

  :deep(.el-input__inner) {
    color: #e2e8f0;
    font-size: 15px;

    &::placeholder {
      color: #475569;
    }
  }

  :deep(.el-input__prefix) {
    color: #64748b;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .remember-checkbox {
    :deep(.el-checkbox__label) {
      color: #94a3b8;
    }
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  background: linear-gradient(135deg, #00d4ff, #7b61ff);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #7b61ff, #00d4ff);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 212, 255, 0.3);

    &::before {
      opacity: 1;
    }
  }

  span {
    position: relative;
    z-index: 1;
  }
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  color: #475569;
  font-size: 13px;
}

// 底部装饰
.bottom-deco {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  pointer-events: none;
  z-index: 5;

  .deco-glow {
    position: absolute;
    bottom: -100px;
    left: 50%;
    transform: translateX(-50%);
    width: 600px;
    height: 200px;
    background: radial-gradient(ellipse, rgba(0, 212, 255, 0.15), transparent 70%);
    animation: bottomGlow 4s ease-in-out infinite;
  }
}

@keyframes bottomGlow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

// 响应式
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    width: 90%;
    min-height: auto;
  }

  .brand-panel {
    padding: 40px 30px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }

  .form-panel {
    padding: 40px 30px;
  }
}
</style>
