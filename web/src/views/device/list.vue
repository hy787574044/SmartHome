<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加设备
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="房间">
          <el-select v-model="queryParams.roomId" placeholder="全部房间" clearable style="width: 160px">
            <el-option v-for="room in rooms" :key="room.roomId" :label="room.roomName" :value="room.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品">
          <el-select v-model="queryParams.productId" placeholder="全部产品" clearable style="width: 160px">
            <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="在线" :value="3" />
            <el-option label="离线" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDevices">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备列表 -->
    <el-card shadow="hover">
      <el-table :data="devices" stripe style="width: 100%">
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="serialNumber" label="序列号" width="200" />
        <el-table-column label="所属产品" width="150">
          <template #default="{ row }">
            {{ getProductName(row.productId) }}
          </template>
        </el-table-column>
        <el-table-column label="所属房间" width="120">
          <template #default="{ row }">
            {{ getRoomName(row.roomId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 3 ? 'success' : row.status === 4 ? 'info' : 'warning'" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后上线" width="180">
          <template #default="{ row }">
            {{ row.lastOnlineTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showControlDialog(row)">控制</el-button>
            <el-button text type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该设备？" @confirm="handleDelete(row.deviceId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadDevices"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '添加设备'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="设备名称" required>
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="序列号" required>
          <el-input v-model="form.serialNumber" placeholder="请输入设备序列号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="所属产品" required>
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%">
            <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属房间">
          <el-select v-model="form.roomId" placeholder="请选择房间" clearable style="width: 100%">
            <el-option v-for="room in rooms" :key="room.roomId" :label="room.roomName" :value="room.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设备控制对话框 -->
    <el-dialog v-model="controlDialogVisible" :title="`控制设备 - ${currentDevice?.deviceName}`" width="400px">
      <div v-if="currentDevice">
        <div v-if="controlFunctions.length === 0" class="empty-tip">
          <el-empty description="该产品暂无功能定义" :image-size="60" />
        </div>
        <div v-else>
          <div v-for="func in controlFunctions" :key="func.identifier" class="control-item">
            <span class="control-label">{{ func.modelName }}</span>
            <div v-if="func.dataType === 'bool'" class="control-action">
              <el-switch
                v-model="controlValues[func.identifier]"
                active-value="1"
                inactive-value="0"
                @change="(val) => handleControl(func.identifier, val)"
              />
            </div>
            <div v-else class="control-action">
              <el-input v-model="controlValues[func.identifier]" style="width: 120px; margin-right: 8px" />
              <el-button type="primary" size="small" @click="handleControl(func.identifier, controlValues[func.identifier])">
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listDevices, addDevice, updateDevice, deleteDevice, controlDevice } from '@/api/device'
import { listAllProducts, listThingsModels } from '@/api/product'
import { listRooms } from '@/api/room'

const queryParams = reactive({ roomId: null, productId: null, status: null, pageNum: 1, pageSize: 10 })
const devices = ref([])
const total = ref(0)
const rooms = ref([])
const products = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ deviceId: null, deviceName: '', serialNumber: '', productId: null, roomId: null, remark: '' })

const controlDialogVisible = ref(false)
const currentDevice = ref(null)
const controlFunctions = ref([])
const controlValues = reactive({})

const statusMap = { 1: '未激活', 2: '禁用', 3: '在线', 4: '离线' }

const getProductName = (id) => products.value.find((p) => p.productId === id)?.productName || '-'
const getRoomName = (id) => rooms.value.find((r) => r.roomId === id)?.roomName || '-'

const loadDevices = async () => {
  const res = await listDevices(queryParams)
  devices.value = res.data.rows
  total.value = res.data.total
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { deviceId: null, deviceName: '', serialNumber: '', productId: null, roomId: null, remark: '' })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.deviceName || !form.serialNumber || !form.productId) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (isEdit.value) {
    await updateDevice(form)
    ElMessage.success('更新成功')
  } else {
    await addDevice(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadDevices()
}

const handleDelete = async (deviceId) => {
  await deleteDevice(deviceId)
  ElMessage.success('删除成功')
  loadDevices()
}

const showControlDialog = async (row) => {
  currentDevice.value = row
  controlDialogVisible.value = true
  controlFunctions.value = []
  Object.keys(controlValues).forEach((k) => delete controlValues[k])

  const res = await listThingsModels(row.productId)
  controlFunctions.value = res.data.filter((m) => m.type === 2)
  controlFunctions.value.forEach((f) => {
    controlValues[f.identifier] = ''
  })
}

const handleControl = async (identifier, value) => {
  try {
    await controlDevice(currentDevice.value.deviceId, identifier, value)
    ElMessage.success('指令已发送')
  } catch (e) {
    // error handled by interceptor
  }
}

onMounted(async () => {
  const [roomsRes, productsRes] = await Promise.all([listRooms(), listAllProducts()])
  rooms.value = roomsRes.data
  products.value = productsRes.data
  loadDevices()
})
</script>

<style lang="scss" scoped>
.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .control-label {
    font-size: 14px;
    color: #303133;
  }

  .control-action {
    display: flex;
    align-items: center;
  }
}
</style>
