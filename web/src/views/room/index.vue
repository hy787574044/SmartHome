<template>
  <div class="page-container">
    <div class="page-header">
      <h2>房间管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加房间
      </el-button>
    </div>

    <div class="card-grid">
      <el-card
        v-for="room in rooms"
        :key="room.roomId"
        shadow="hover"
        class="room-card"
      >
        <template #header>
          <div class="room-header">
            <div class="room-info">
              <el-icon size="20"><House /></el-icon>
              <span class="room-name">{{ room.roomName }}</span>
              <el-tag size="small" type="info">{{ roomTypeMap[room.roomType] || room.roomType }}</el-tag>
            </div>
            <div class="room-actions">
              <el-button text type="primary" size="small" @click="showEditDialog(room)">编辑</el-button>
              <el-popconfirm title="确定删除该房间？" @confirm="handleDelete(room.roomId)">
                <template #reference>
                  <el-button text type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </template>
        <div class="room-detail">
          <div class="detail-item">
            <span class="label">楼层：</span>
            <span>{{ room.floor || '-' }} 层</span>
          </div>
          <div class="detail-item">
            <span class="label">设备数：</span>
            <span>{{ getDeviceCount(room.roomId) }} 个</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房间' : '添加房间'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="房间名称" required>
          <el-input v-model="form.roomName" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="form.roomType" placeholder="请选择" style="width: 100%">
            <el-option label="客厅" value="living_room" />
            <el-option label="卧室" value="bedroom" />
            <el-option label="厨房" value="kitchen" />
            <el-option label="卫生间" value="bathroom" />
            <el-option label="书房" value="study" />
            <el-option label="阳台" value="balcony" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="form.floor" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listRooms, addRoom, updateRoom, deleteRoom } from '@/api/room'
import { listDevices } from '@/api/device'

const rooms = ref([])
const devices = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ roomId: null, roomName: '', roomType: '', floor: 1, sortOrder: 0, remark: '' })

const roomTypeMap = {
  living_room: '客厅',
  bedroom: '卧室',
  kitchen: '厨房',
  bathroom: '卫生间',
  study: '书房',
  balcony: '阳台',
}

const getDeviceCount = (roomId) => devices.value.filter((d) => d.roomId === roomId).length

const loadData = async () => {
  const [roomsRes, devicesRes] = await Promise.all([listRooms(), listDevices({ pageSize: 1000 })])
  rooms.value = roomsRes.data
  devices.value = devicesRes.data.rows
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { roomId: null, roomName: '', roomType: '', floor: 1, sortOrder: 0, remark: '' })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.roomName) {
    ElMessage.warning('请输入房间名称')
    return
  }
  if (isEdit.value) {
    await updateRoom(form)
    ElMessage.success('更新成功')
  } else {
    await addRoom(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (roomId) => {
  await deleteRoom(roomId)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.room-card {
  .room-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .room-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .room-name {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .room-detail {
    .detail-item {
      margin-bottom: 8px;
      font-size: 14px;
      color: #f0f4f8;

      .label {
        color: #94a3b8;
      }
    }
  }
}
</style>
