<template>
  <div class="profile-page">
    <!-- Decorative grid background -->
    <div class="grid-bg"></div>
    <div class="glow-orb glow-orb-1"></div>
    <div class="glow-orb glow-orb-2"></div>

    <div class="profile-layout">
      <!-- ========== Left: User Card ========== -->
      <div class="user-card">
        <div class="user-card__glow"></div>
        <div class="user-card__header-decor"></div>

        <!-- Avatar -->
        <div class="user-card__avatar-wrap" @click="triggerUpload">
          <div class="user-card__avatar-ring"></div>
          <el-avatar :size="96" :src="profileForm.avatar || undefined" class="user-card__avatar">
            {{ profileForm.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="user-card__avatar-overlay">
            <el-icon :size="20"><Camera /></el-icon>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </div>

        <!-- User Info -->
        <div class="user-card__name">{{ profileForm.nickname || profileForm.username }}</div>
        <div class="user-card__username">@{{ profileForm.username }}</div>

        <!-- Role Badge -->
        <div class="user-card__role">
          <el-tag effect="dark" :type="roleTagType" class="role-tag">
            <el-icon :size="12"><UserFilled /></el-icon>
            {{ roleLabel }}
          </el-tag>
        </div>

        <!-- Divider -->
        <div class="user-card__divider"></div>

        <!-- Quick Stats -->
        <div class="user-card__stats">
          <div class="user-card__stat">
            <div class="user-card__stat-value">{{ profileForm.email || '--' }}</div>
            <div class="user-card__stat-label">邮箱</div>
          </div>
          <div class="user-card__stat">
            <div class="user-card__stat-value">{{ profileForm.phone || '--' }}</div>
            <div class="user-card__stat-label">手机</div>
          </div>
        </div>
      </div>

      <!-- ========== Right: Forms ========== -->
      <div class="form-area">
        <!-- Profile Form Panel -->
        <div class="panel">
          <div class="panel__header">
            <div class="panel__title">
              <span class="panel__title-icon">
                <el-icon><User /></el-icon>
              </span>
              基本信息
            </div>
            <div class="panel__header-decor"></div>
          </div>
          <div class="panel__body">
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="80px"
              class="profile-form"
            >
              <el-form-item label="昵称" prop="nickname">
                <el-input
                  v-model="profileForm.nickname"
                  placeholder="请输入昵称"
                  :prefix-icon="UserIcon"
                />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  placeholder="请输入邮箱"
                  :prefix-icon="Message"
                />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Iphone"
                />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-select v-model="profileForm.gender" placeholder="请选择性别" style="width: 100%">
                  <el-option label="男" :value="1">
                    <el-icon style="margin-right: 6px; vertical-align: middle"><Male /></el-icon>
                    男
                  </el-option>
                  <el-option label="女" :value="2">
                    <el-icon style="margin-right: 6px; vertical-align: middle"><Female /></el-icon>
                    女
                  </el-option>
                  <el-option label="保密" :value="0">
                    <el-icon style="margin-right: 6px; vertical-align: middle"><Hide /></el-icon>
                    保密
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="profileSaving"
                  class="save-btn"
                  @click="handleSaveProfile"
                >
                  <el-icon v-if="!profileSaving"><Check /></el-icon>
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- Password Form Panel -->
        <div class="panel">
          <div class="panel__header">
            <div class="panel__title">
              <span class="panel__title-icon panel__title-icon--warn">
                <el-icon><Lock /></el-icon>
              </span>
              修改密码
            </div>
            <div class="panel__header-decor"></div>
          </div>
          <div class="panel__body">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入旧密码"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（至少6位）"
                  show-password
                  :prefix-icon="Key"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  :prefix-icon="Key"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="warning"
                  :loading="passwordSaving"
                  class="save-btn save-btn--warn"
                  @click="handleChangePassword"
                >
                  <el-icon v-if="!passwordSaving"><Lock /></el-icon>
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, updateUserProfile, changePassword } from '@/api/user'
import {
  User, UserFilled, Lock, Key, Check, Camera, Message, Iphone,
  Male, Female, Hide,
} from '@element-plus/icons-vue'

// ---- Refs ----
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const fileInputRef = ref(null)
const profileSaving = ref(false)
const passwordSaving = ref(false)

// ---- Profile Form ----
const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 0,
  role: '',
})

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

// ---- Password Form ----
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

// ---- Computed ----
const roleLabel = computed(() => {
  const map = { admin: '管理员', user: '普通用户', device: '设备管理员' }
  return map[profileForm.role] || profileForm.role || '用户'
})

const roleTagType = computed(() => {
  const map = { admin: 'danger', device: 'warning', user: '' }
  return map[profileForm.role] || 'info'
})

// ---- Avatar Upload ----
const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像文件大小不能超过 2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    profileForm.avatar = ev.target.result
    ElMessage.success('头像已更新，点击保存修改以生效')
  }
  reader.readAsDataURL(file)
  // Reset input so the same file can be selected again
  e.target.value = ''
}

// ---- Load Profile ----
const loadProfile = async () => {
  try {
    const res = await getUserProfile()
    Object.assign(profileForm, res.data)
  } catch {
    // Fallback: try localStorage
    const cached = JSON.parse(localStorage.getItem('userInfo') || '{}')
    Object.assign(profileForm, cached)
  }
}

// ---- Save Profile ----
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileSaving.value = true
  try {
    await updateUserProfile({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
      gender: profileForm.gender,
    })
    localStorage.setItem('userInfo', JSON.stringify(profileForm))
    ElMessage.success('个人信息保存成功')
  } catch {
    // Error already handled by interceptor
  } finally {
    profileSaving.value = false
  }
}

// ---- Change Password ----
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordFormRef.value.resetFields()
  } catch {
    // Error already handled by interceptor
  } finally {
    passwordSaving.value = false
  }
}

// ---- Init ----
onMounted(() => {
  loadProfile()
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

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.15); opacity: 0; }
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* ===== Page Root ===== */
.profile-page {
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

/* ===== Layout ===== */
.profile-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto;
  animation: fadeSlideUp 0.6s ease both;
}

/* ===== User Card (Left) ===== */
.user-card {
  position: relative;
  background: $bg-card;
  border: 1px solid $border-glow;
  border-radius: $radius;
  backdrop-filter: blur(12px);
  padding: 36px 24px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  animation: fadeSlideUp 0.6s ease 0.1s both;

  &__glow {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, $cyan, $blue, transparent);
    border-radius: $radius $radius 0 0;
  }

  &__header-decor {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 100px;
    background: linear-gradient(180deg, rgba($cyan, 0.04), transparent);
    pointer-events: none;
  }

  /* Avatar */
  &__avatar-wrap {
    position: relative;
    cursor: pointer;
    margin-bottom: 16px;

    &:hover .user-card__avatar-overlay {
      opacity: 1;
    }
    &:hover .user-card__avatar-ring {
      animation: ringPulse 1.5s ease-in-out infinite;
    }
  }

  &__avatar-ring {
    position: absolute;
    inset: -6px;
    border-radius: 50%;
    border: 2px solid rgba($cyan, 0.3);
    pointer-events: none;
  }

  &__avatar {
    border: 3px solid rgba($cyan, 0.2);
    background: rgba($cyan, 0.08);
    font-size: 36px;
    font-weight: 700;
    color: $cyan;
    transition: transform 0.3s;

    .user-card__avatar-wrap:hover & {
      transform: scale(1.05);
    }
  }

  &__avatar-overlay {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    opacity: 0;
    transition: opacity 0.3s;
  }

  /* Name & Username */
  &__name {
    font-size: 20px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4px;
  }

  &__username {
    font-size: 13px;
    color: $text-dim;
    margin-bottom: 14px;
  }

  /* Role Badge */
  &__role {
    margin-bottom: 20px;
  }

  .role-tag {
    border-radius: 20px;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    background: rgba($cyan, 0.1);
    border-color: rgba($cyan, 0.2);
    color: $cyan;
  }

  /* Divider */
  &__divider {
    width: 100%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($cyan, 0.15), transparent);
    margin-bottom: 20px;
  }

  /* Stats */
  &__stats {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  &__stat {
    text-align: center;

    &-value {
      font-size: 13px;
      color: $text-primary;
      font-weight: 500;
      word-break: break-all;
    }

    &-label {
      font-size: 11px;
      color: $text-dim;
      margin-top: 2px;
    }
  }
}

/* ===== Form Area (Right) ===== */
.form-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== Panel ===== */
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
    padding: 24px 22px;
  }
}

/* ===== Form Overrides (dark theme) ===== */
.profile-form {
  max-width: 520px;

  :deep(.el-form-item__label) {
    color: $text-secondary;
    font-weight: 500;
  }

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.08);
    box-shadow: none !important;
    border-radius: 8px;
    transition: border-color 0.3s;

    &:hover {
      border-color: rgba($cyan, 0.3);
    }

    &.is-focus {
      border-color: $cyan;
      box-shadow: 0 0 0 2px rgba($cyan, 0.1) !important;
    }
  }

  :deep(.el-input__inner) {
    color: $text-primary;

    &::placeholder {
      color: $text-dim;
    }
  }

  :deep(.el-input__prefix) {
    color: $text-dim;
  }

  :deep(.el-input__suffix) {
    color: $text-dim;
  }

  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-select .el-input__wrapper) {
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.08);
    box-shadow: none !important;
  }

  :deep(.el-select-dropdown) {
    background: $bg-secondary;
    border: 1px solid $border-glow;
  }

  :deep(.el-select-dropdown__item) {
    color: $text-primary;

    &.hover, &:hover {
      background: rgba($cyan, 0.08);
    }

    &.is-selected {
      color: $cyan;
      font-weight: 600;
    }
  }

  :deep(.el-form-item__error) {
    font-size: 12px;
  }
}

/* ===== Save Button ===== */
.save-btn {
  min-width: 140px;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: linear-gradient(135deg, $cyan, $blue);
  color: #fff;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba($cyan, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  &--warn {
    background: linear-gradient(135deg, $orange, #e68a00);

    &:hover {
      box-shadow: 0 4px 20px rgba($orange, 0.3);
    }
  }
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .user-card {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
    padding: 24px 20px;

    &__avatar-wrap {
      margin-bottom: 0;
    }

    &__stats {
      flex-direction: row;
      gap: 24px;
    }

    &__divider {
      margin-bottom: 12px;
    }
  }
}

@media (max-width: 600px) {
  .profile-page {
    padding: 12px;
  }

  .profile-form {
    max-width: 100%;
  }
}
</style>
