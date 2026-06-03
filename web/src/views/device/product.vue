<template>
  <div class="page-container">
    <div class="page-header">
      <h2>产品管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加产品
      </el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="products" stripe>
        <el-table-column prop="productName" label="产品名称" width="200" />
        <el-table-column label="设备类型" width="120">
          <template #default="{ row }">
            {{ deviceTypeMap[row.deviceType] }}
          </template>
        </el-table-column>
        <el-table-column prop="networkMethod" label="联网方式" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showModelDialog(row)">物模型</el-button>
            <el-button text type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该产品？" @confirm="handleDelete(row.productId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑产品对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品' : '添加产品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="产品名称" required>
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="form.deviceType" style="width: 100%">
            <el-option label="直连设备" :value="1" />
            <el-option label="网关" :value="2" />
            <el-option label="监测设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="联网方式">
          <el-select v-model="form.networkMethod" style="width: 100%">
            <el-option label="WiFi" value="wifi" />
            <el-option label="Zigbee" value="zigbee" />
            <el-option label="蓝牙" value="ble" />
            <el-option label="以太网" value="ethernet" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="产品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 物模型管理对话框 -->
    <el-dialog v-model="modelDialogVisible" :title="`物模型 - ${currentProduct?.productName}`" width="800px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" size="small" @click="showAddModel">添加物模型</el-button>
      </div>
      <el-table :data="thingsModels" stripe size="small">
        <el-table-column prop="modelName" label="名称" width="120" />
        <el-table-column prop="identifier" label="标识符" width="150" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? '' : row.type === 2 ? 'success' : 'warning'" size="small">
              {{ modelTypeMap[row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataType" label="数据类型" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="只读" width="80">
          <template #default="{ row }">
            {{ row.readonly === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showEditModel(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDeleteModel(row.modelId)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 物模型表单 -->
      <el-dialog v-model="modelFormVisible" :title="isEditModel ? '编辑物模型' : '添加物模型'" width="500px" append-to-body>
        <el-form :model="modelForm" label-width="80px">
          <el-form-item label="名称" required>
            <el-input v-model="modelForm.modelName" placeholder="如：温度、开关" />
          </el-form-item>
          <el-form-item label="标识符" required>
            <el-input v-model="modelForm.identifier" placeholder="如：temperature、switch" />
          </el-form-item>
          <el-form-item label="类型" required>
            <el-select v-model="modelForm.type" style="width: 100%">
              <el-option label="属性 (Property)" :value="1" />
              <el-option label="功能 (Function)" :value="2" />
              <el-option label="事件 (Event)" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据类型">
            <el-select v-model="modelForm.dataType" style="width: 100%">
              <el-option label="整数 (integer)" value="integer" />
              <el-option label="小数 (decimal)" value="decimal" />
              <el-option label="字符串 (string)" value="string" />
              <el-option label="布尔 (bool)" value="bool" />
              <el-option label="枚举 (enum)" value="enum" />
            </el-select>
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="modelForm.unit" placeholder="如：℃、%" />
          </el-form-item>
          <el-form-item label="只读">
            <el-switch v-model="modelForm.readonly" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="首页展示">
            <el-switch v-model="modelForm.showIndex" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="modelFormVisible = false">取消</el-button>
          <el-button type="primary" @click="handleModelSubmit">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, addProduct, updateProduct, deleteProduct, listThingsModels, addThingsModel, updateThingsModel, deleteThingsModel } from '@/api/product'

const products = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ productId: null, productName: '', deviceType: 1, networkMethod: 'wifi', description: '' })

const modelDialogVisible = ref(false)
const modelFormVisible = ref(false)
const isEditModel = ref(false)
const currentProduct = ref(null)
const thingsModels = ref([])
const modelForm = reactive({ modelId: null, productId: null, modelName: '', identifier: '', type: 1, dataType: 'string', unit: '', readonly: 0, showIndex: 0, sortOrder: 0 })

const deviceTypeMap = { 1: '直连设备', 2: '网关', 3: '监测设备' }
const modelTypeMap = { 1: '属性', 2: '功能', 3: '事件' }

const loadProducts = async () => {
  const res = await listProducts({ pageSize: 100 })
  products.value = res.data.rows
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { productId: null, productName: '', deviceType: 1, networkMethod: 'wifi', description: '' })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.productName) {
    ElMessage.warning('请输入产品名称')
    return
  }
  if (isEdit.value) {
    await updateProduct(form)
    ElMessage.success('更新成功')
  } else {
    await addProduct(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadProducts()
}

const handleDelete = async (productId) => {
  await deleteProduct(productId)
  ElMessage.success('删除成功')
  loadProducts()
}

const showModelDialog = async (product) => {
  currentProduct.value = product
  modelDialogVisible.value = true
  const res = await listThingsModels(product.productId)
  thingsModels.value = res.data
}

const showAddModel = () => {
  isEditModel.value = false
  Object.assign(modelForm, { modelId: null, productId: currentProduct.value.productId, modelName: '', identifier: '', type: 1, dataType: 'string', unit: '', readonly: 0, showIndex: 0, sortOrder: 0 })
  modelFormVisible.value = true
}

const showEditModel = (row) => {
  isEditModel.value = true
  Object.assign(modelForm, row)
  modelFormVisible.value = true
}

const handleModelSubmit = async () => {
  if (!modelForm.modelName || !modelForm.identifier) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (isEditModel.value) {
    await updateThingsModel(modelForm)
    ElMessage.success('更新成功')
  } else {
    await addThingsModel(modelForm)
    ElMessage.success('添加成功')
  }
  modelFormVisible.value = false
  const res = await listThingsModels(currentProduct.value.productId)
  thingsModels.value = res.data
}

const handleDeleteModel = async (modelId) => {
  await deleteThingsModel(modelId)
  ElMessage.success('删除成功')
  const res = await listThingsModels(currentProduct.value.productId)
  thingsModels.value = res.data
}

onMounted(() => loadProducts())
</script>
