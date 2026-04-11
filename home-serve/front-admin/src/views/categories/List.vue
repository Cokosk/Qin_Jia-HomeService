<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">分类管理</h2>

    <!-- 操作栏 -->
    <div class="card" style="margin-bottom: 16px;">
      <div style="padding: 16px; display: flex; gap: 16px; align-items: center;">
        <el-button type="success" @click="showAddDialog">添加分类</el-button>
      </div>
    </div>

    <!-- 分类列表 -->
    <div class="card">
      <el-table :data="categories" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <span v-if="row.icon">{{ row.icon }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" width="180" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '添加分类'" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="图标类名或emoji" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref({
  id: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

onMounted(() => {
  loadCategories()
})

async function loadCategories() {
  try {
    const res = await api.admin.getCategoryList()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

function showAddDialog() {
  isEdit.value = false
  formData.value = { id: null, name: '', icon: '', sort: 0, status: 1 }
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

async function submitCategory() {
  try {
    const res = isEdit.value
      ? await api.admin.updateCategory(formData.value)
      : await api.admin.addCategory(formData.value)
    
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      loadCategories()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await api.admin.updateCategoryStatus(row.id, newStatus)
  if (res.code === 200) {
    ElMessage.success(res.message)
    row.status = newStatus
  } else {
    ElMessage.error(res.message)
  }
}

async function deleteCategory(row) {
  try {
    await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
    const res = await api.admin.deleteCategory(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadCategories()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>