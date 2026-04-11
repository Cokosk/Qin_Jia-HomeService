<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">服务管理</h2>

    <!-- 操作栏 -->
    <div class="card" style="margin-bottom: 16px;">
      <div style="padding: 16px; display: flex; gap: 16px; align-items: center;">
        <el-select v-model="categoryFilter" placeholder="分类筛选" clearable style="width: 150px;">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;">
          <el-option label="上架" value="1" />
          <el-option label="下架" value="0" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索服务名称" style="width: 200px;" />
        <el-button type="primary" @click="loadServices">搜索</el-button>
        <el-button type="success" @click="showAddDialog">添加服务</el-button>
      </div>
    </div>

    <!-- 服务列表 -->
    <div class="card">
      <el-table :data="services" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="服务名称" width="180" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="orderCount" label="订单数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteService(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="pageNum = $event; loadServices()"
        />
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务' : '添加服务'" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="服务名称">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.categoryId" style="width: 100%;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="formData.price" :min="0" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="如：次/台/车" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitService">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const services = ref([])
const categories = ref([])
const keyword = ref('')
const categoryFilter = ref(null)
const statusFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  unit: '次',
  description: '',
  status: 1
})

onMounted(async () => {
  const catRes = await api.service.getCategories()
  if (catRes.code === 200) categories.value = catRes.data
  loadServices()
})

async function loadServices() {
  try {
    const res = await api.admin.getServiceList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      categoryId: categoryFilter.value,
      status: statusFilter.value
    })
    if (res.code === 200) {
      services.value = res.data
      total.value = res.total
    }
  } catch (e) {
    console.error(e)
  }
}

function showAddDialog() {
  isEdit.value = false
  formData.value = { id: null, name: '', categoryId: null, price: 0, unit: '次', description: '', status: 1 }
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

async function submitService() {
  try {
    const res = isEdit.value
      ? await api.admin.updateService(formData.value)
      : await api.admin.addService(formData.value)
    
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      loadServices()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await api.admin.updateServiceStatus(row.id, newStatus)
  if (res.code === 200) {
    ElMessage.success(res.message)
    row.status = newStatus
  } else {
    ElMessage.error(res.message)
  }
}

async function deleteService(row) {
  try {
    await ElMessageBox.confirm('确认删除该服务？', '提示', { type: 'warning' })
    const res = await api.admin.deleteService(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadServices()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>
