<template>
  <div class="family-page">
    <div class="grid-bg"></div>
    <div class="glow-orb glow-orb-1"></div>
    <div class="glow-orb glow-orb-2"></div>

    <div class="family-layout">
      <!-- ========== 无家庭状态：创建 / 加入 ========== -->
      <template v-if="!hasFamily">
        <div class="no-family-container">
          <!-- 创建家庭 -->
          <div class="action-card">
            <div class="action-card__glow"></div>
            <div class="action-card__icon-wrap">
              <el-icon :size="32"><HomeFilled /></el-icon>
            </div>
            <h3 class="action-card__title">创建家庭</h3>
            <p class="action-card__desc">创建一个新的家庭，邀请成员加入</p>
            <el-form
              ref="createFormRef"
              :model="createForm"
              :rules="createRules"
              class="action-card__form"
            >
              <el-form-item prop="name">
                <el-input
                  v-model="createForm.name"
                  placeholder="请输入家庭名称"
                  :prefix-icon="HomeFilled"
                  size="large"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="creating"
                  class="action-btn"
                  @click="handleCreate"
                >
                  <el-icon v-if="!creating"><Plus /></el-icon>
                  创建家庭
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 加入家庭 -->
          <div class="action-card">
            <div class="action-card__glow action-card__glow--purple"></div>
            <div class="action-card__icon-wrap action-card__icon-wrap--purple">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <h3 class="action-card__title">加入家庭</h3>
            <p class="action-card__desc">输入邀请码加入已有家庭</p>
            <el-form
              ref="joinFormRef"
              :model="joinForm"
              :rules="joinRules"
              class="action-card__form"
            >
              <el-form-item prop="inviteCode">
                <el-input
                  v-model="joinForm.inviteCode"
                  placeholder="请输入邀请码"
                  :prefix-icon="Key"
                  size="large"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="joining"
                  class="action-btn action-btn--purple"
                  @click="handleJoin"
                >
                  <el-icon v-if="!joining"><Right /></el-icon>
                  加入家庭
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>

      <!-- ========== 有家庭状态：信息 + 成员 ========== -->
      <template v-else>
        <!-- 顶部卡片行 -->
        <div class="info-row">
          <!-- 家庭信息卡片 -->
          <div class="info-card info-card--primary">
            <div class="info-card__glow"></div>
            <div class="info-card__header">
              <div class="info-card__icon-wrap">
                <el-icon :size="24"><HomeFilled /></el-icon>
              </div>
              <div class="info-card__header-text">
                <h2 class="info-card__title">{{ familyInfo.name }}</h2>
                <span class="info-card__subtitle">家庭信息</span>
              </div>
            </div>
            <div class="info-card__body">
              <div class="info-card__stat">
                <span class="info-card__stat-value">{{ memberCount }}</span>
                <span class="info-card__stat-label">成员数量</span>
              </div>
              <div class="info-card__stat">
                <span class="info-card__stat-value info-card__stat-value--code">
                  {{ familyInfo.inviteCode }}
                  <el-icon class="copy-icon" @click="copyInviteCode"><CopyDocument /></el-icon>
                </span>
                <span class="info-card__stat-label">邀请码</span>
              </div>
            </div>
          </div>

          <!-- 邀请区域卡片 -->
          <div class="info-card info-card--invite">
            <div class="info-card__glow info-card__glow--purple"></div>
            <div class="info-card__header">
              <div class="info-card__icon-wrap info-card__icon-wrap--purple">
                <el-icon :size="24"><Promotion /></el-icon>
              </div>
              <div class="info-card__header-text">
                <h2 class="info-card__title">邀请成员</h2>
                <span class="info-card__subtitle">分享邀请码让家人加入</span>
              </div>
            </div>
            <div class="info-card__body info-card__body--invite">
              <div class="invite-code-display">
                <span class="invite-code-text">{{ familyInfo.inviteCode }}</span>
              </div>
              <div class="invite-actions">
                <el-button type="primary" class="invite-btn" @click="copyInviteCode">
                  <el-icon><CopyDocument /></el-icon>
                  复制邀请码
                </el-button>
                <el-button type="warning" class="invite-btn invite-btn--warn" :loading="refreshing" @click="handleRefreshCode">
                  <el-icon v-if="!refreshing"><Refresh /></el-icon>
                  刷新邀请码
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 成员列表 -->
        <div class="panel">
          <div class="panel__header">
            <div class="panel__title">
              <span class="panel__title-icon">
                <el-icon><User /></el-icon>
              </span>
              成员列表
            </div>
            <div class="panel__header-decor"></div>
          </div>
          <div class="panel__body">
            <el-table
              :data="members"
              class="member-table"
              :header-cell-style="{ background: 'rgba(0, 240, 255, 0.04)', color: '#94a3b8', borderColor: 'rgba(0, 240, 255, 0.08)' }"
              :cell-style="{ borderColor: 'rgba(0, 240, 255, 0.06)' }"
            >
              <el-table-column label="成员" min-width="200">
                <template #default="{ row }">
                  <div class="member-info">
                    <el-avatar :size="36" :src="row.avatar || undefined" class="member-avatar">
                      {{ row.username?.charAt(0)?.toUpperCase() }}
                    </el-avatar>
                    <div class="member-text">
                      <span class="member-name">{{ row.nickname || row.username }}</span>
                      <span class="member-username">@{{ row.username }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="角色" width="140">
                <template #default="{ row }">
                  <el-tag
                    :type="roleTagType(row.role)"
                    effect="dark"
                    class="role-tag"
                  >
                    {{ roleLabel(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="加入时间" prop="joinTime" width="180" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <div class="action-cell" v-if="row.role !== 'owner'">
                    <el-select
                      v-if="isOwner"
                      :model-value="row.role"
                      size="small"
                      class="role-select"
                      @change="(val) => handleChangeRole(row.id, val)"
                    >
                      <el-option label="管理员" value="admin" />
                      <el-option label="成员" value="member" />
                      <el-option label="访客" value="guest" />
                    </el-select>
                    <el-button
                      v-if="isOwner"
                      type="danger"
                      size="small"
                      link
                      @click="handleRemove(row)"
                    >
                      <el-icon><Delete /></el-icon>
                      移除
                    </el-button>
                  </div>
                  <span v-else class="owner-label">创建者</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled, UserFilled, User, Plus, Right, Key,
  CopyDocument, Refresh, Delete, Promotion,
} from '@element-plus/icons-vue'
import {
  createFamily, joinFamily, getFamilyInfo, getMembers,
  removeMember, updateMemberRole, refreshInviteCode,
} from '@/api/family'

// ---- State ----
const hasFamily = ref(false)
const creating = ref(false)
const joining = ref(false)
const refreshing = ref(false)
const createFormRef = ref(null)
const joinFormRef = ref(null)

const familyInfo = reactive({
  name: '',
  inviteCode: '',
  ownerId: null,
})

const members = ref([])

const memberCount = computed(() => members.value.length)
const currentUserId = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}').id
  } catch {
    return null
  }
})
const isOwner = computed(() => currentUserId.value === familyInfo.ownerId)

// ---- Create Form ----
const createForm = reactive({ name: '' })
const createRules = {
  name: [
    { required: true, message: '请输入家庭名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
}

// ---- Join Form ----
const joinForm = reactive({ inviteCode: '' })
const joinRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
  ],
}

// ---- Helpers ----
const roleLabel = (role) => {
  const map = { owner: '创建者', admin: '管理员', member: '成员', guest: '访客' }
  return map[role] || role
}

const roleTagType = (role) => {
  const map = { owner: 'danger', admin: 'warning', member: '', guest: 'info' }
  return map[role] || 'info'
}

// ---- Actions ----
const handleCreate = async () => {
  if (!createFormRef.value) return
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createFamily({ name: createForm.name })
    ElMessage.success('家庭创建成功')
    await loadData()
  } catch {
    // interceptor handles error
  } finally {
    creating.value = false
  }
}

const handleJoin = async () => {
  if (!joinFormRef.value) return
  const valid = await joinFormRef.value.validate().catch(() => false)
  if (!valid) return

  joining.value = true
  try {
    await joinFamily({ inviteCode: joinForm.inviteCode })
    ElMessage.success('成功加入家庭')
    await loadData()
  } catch {
    // interceptor handles error
  } finally {
    joining.value = false
  }
}

const copyInviteCode = () => {
  if (!familyInfo.inviteCode) return
  navigator.clipboard.writeText(familyInfo.inviteCode).then(() => {
    ElMessage.success('邀请码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const handleRefreshCode = async () => {
  try {
    await ElMessageBox.confirm('刷新后旧邀请码将失效，确定继续？', '刷新邀请码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  refreshing.value = true
  try {
    const res = await refreshInviteCode()
    familyInfo.inviteCode = res.data?.inviteCode || ''
    ElMessage.success('邀请码已刷新')
  } catch {
    // interceptor handles error
  } finally {
    refreshing.value = false
  }
}

const handleChangeRole = async (memberId, role) => {
  try {
    await updateMemberRole(memberId, role)
    ElMessage.success('角色修改成功')
    await loadMembers()
  } catch {
    // interceptor handles error
  }
}

const handleRemove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员「${row.nickname || row.username}」吗？`,
      '移除成员',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }

  try {
    await removeMember(row.id)
    ElMessage.success('成员已移除')
    await loadMembers()
  } catch {
    // interceptor handles error
  }
}

// ---- Data Loading ----
const loadMembers = async () => {
  try {
    const res = await getMembers()
    members.value = res.data || []
  } catch {
    // interceptor handles error
  }
}

const loadData = async () => {
  try {
    const res = await getFamilyInfo()
    if (res.data) {
      Object.assign(familyInfo, res.data)
      hasFamily.value = true
      await loadMembers()
    }
  } catch {
    hasFamily.value = false
  }
}

// ---- Init ----
onMounted(() => {
  loadData()
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

/* ===== Page Root ===== */
.family-page {
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
.family-layout {
  position: relative;
  z-index: 1;
  max-width: 1100px;
  margin: 0 auto;
  animation: fadeSlideUp 0.6s ease both;
}

/* ===== No Family: Create / Join ===== */
.no-family-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  padding-top: 40px;
}

.action-card {
  position: relative;
  background: $bg-card;
  border: 1px solid $border-glow;
  border-radius: $radius;
  backdrop-filter: blur(12px);
  padding: 36px 28px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  overflow: hidden;

  &__glow {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, $cyan, $blue, transparent);
    border-radius: $radius $radius 0 0;

    &--purple {
      background: linear-gradient(90deg, transparent, $purple, $blue, transparent);
    }
  }

  &__icon-wrap {
    width: 64px; height: 64px;
    border-radius: 16px;
    background: rgba($cyan, 0.12);
    color: $cyan;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;

    &--purple {
      background: rgba($purple, 0.12);
      color: $purple;
    }
  }

  &__title {
    font-size: 20px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 14px;
    color: $text-dim;
    margin-bottom: 24px;
  }

  &__form {
    width: 100%;
    max-width: 320px;

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
  }
}

.action-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  background: linear-gradient(135deg, $cyan, $blue);
  color: #fff;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba($cyan, 0.3);
  }

  &--purple {
    background: linear-gradient(135deg, $purple, $blue);

    &:hover {
      box-shadow: 0 4px 20px rgba($purple, 0.3);
    }
  }
}

/* ===== Info Row (has family) ===== */
.info-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.info-card {
  position: relative;
  background: $bg-card;
  border: 1px solid $border-glow;
  border-radius: $radius;
  backdrop-filter: blur(12px);
  padding: 24px;
  overflow: hidden;

  &__glow {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, $cyan, $blue, transparent);
    border-radius: $radius $radius 0 0;

    &--purple {
      background: linear-gradient(90deg, transparent, $purple, $blue, transparent);
    }
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 20px;
  }

  &__icon-wrap {
    width: 44px; height: 44px;
    border-radius: 12px;
    background: rgba($cyan, 0.12);
    color: $cyan;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--purple {
      background: rgba($purple, 0.12);
      color: $purple;
    }
  }

  &__header-text {
    display: flex;
    flex-direction: column;
  }

  &__title {
    font-size: 18px;
    font-weight: 700;
    color: $text-primary;
    margin: 0;
  }

  &__subtitle {
    font-size: 13px;
    color: $text-dim;
  }

  &__body {
    display: flex;
    gap: 32px;

    &--invite {
      flex-direction: column;
      gap: 16px;
    }
  }

  &__stat {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__stat-value {
    font-size: 22px;
    font-weight: 700;
    color: $text-primary;

    &--code {
      font-size: 16px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-family: 'Courier New', monospace;
      letter-spacing: 2px;
      color: $cyan;
    }
  }

  &__stat-label {
    font-size: 12px;
    color: $text-dim;
  }
}

.copy-icon {
  cursor: pointer;
  color: $text-dim;
  transition: color 0.3s;

  &:hover {
    color: $cyan;
  }
}

.invite-code-display {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
}

.invite-code-text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
  color: $cyan;
}

.invite-actions {
  display: flex;
  gap: 12px;
}

.invite-btn {
  flex: 1;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  &--warn {
    background: linear-gradient(135deg, $orange, #e68a00);
    color: #fff;

    &:hover {
      box-shadow: 0 4px 20px rgba($orange, 0.3);
    }
  }
}

/* ===== Panel (Member Table) ===== */
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
  }

  &__body {
    padding: 16px;
  }
}

/* ===== Member Table ===== */
.member-table {
  width: 100%;
  background: transparent !important;

  :deep(.el-table__inner-wrapper::before),
  :deep(.el-table__border-left-patch) {
    display: none;
  }

  :deep(.el-table__body-wrapper) {
    background: transparent;
  }

  :deep(tr) {
    background: transparent !important;

    &:hover > td {
      background: rgba($cyan, 0.04) !important;
    }
  }

  :deep(td) {
    background: transparent !important;
  }

  :deep(.el-table__empty-text) {
    color: $text-dim;
  }
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar {
  background: rgba($cyan, 0.1);
  color: $cyan;
  font-weight: 700;
  font-size: 14px;
  border: 2px solid rgba($cyan, 0.15);
  flex-shrink: 0;
}

.member-text {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.member-username {
  font-size: 12px;
  color: $text-dim;
}

.role-tag {
  border-radius: 20px;
  font-size: 12px;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-select {
  width: 100px;

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.08);
    box-shadow: none !important;
    border-radius: 6px;
  }

  :deep(.el-input__inner) {
    color: $text-primary;
    font-size: 12px;
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
    }
  }
}

.owner-label {
  font-size: 12px;
  color: $text-dim;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .no-family-container,
  .info-row {
    grid-template-columns: 1fr;
  }

  .invite-actions {
    flex-direction: column;
  }
}

@media (max-width: 600px) {
  .family-page {
    padding: 12px;
  }

  .info-card__body {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
