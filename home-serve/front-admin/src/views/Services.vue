<template>
  <div>
    <div class="page-card">
      <div class="page-title">服务管理</div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="服务分类" name="category">
          <el-button type="primary" @click="addCategory">新增分类</el-button>
          <el-table :data="categories" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="分类名称" />
            <el-table-column prop="icon" label="图标" width="120" />
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="服务项目" name="service">
          <el-form :inline="true" class="search-form">
            <el-form-item label="分类">
              <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable>
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadServices">搜索</el-button>
              <el-button type="primary" @click="addService">新增服务</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="services" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="服务名称" />
            <el-table-column prop="categoryId" label="分类" width="100">
              <template #default="{ row }">
                {{ getCategoryName(row.categoryId) }}
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="editService(row)">编辑</el-button>
                <el-button type="danger" link @click="deleteService(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serviceApi } from '../api'

const activeTab = ref('category')
const categories = ref([])
const services = ref([])
const searchForm = ref({ categoryId: '' })

const getCategoryName = (id) => {
  const c = categories.value.find(c => c.id === id)
  return c ? c.name : '-'
}

const loadCategories = async () => {
  try {
    const res = await serviceApi.categoryList()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载分类失败')
  }
}

const loadServices = async () => {
  try {
    const res = await serviceApi.list(searchForm.value.categoryId ? { categoryId: searchForm.value.categoryId } : {})
    if (res.code === 200) {
      services.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载服务失败')
  }
}

const addCategory = () => {
  ElMessage.info('功能开发中')
}

const addService = () => {
  ElMessage.info('功能开发中')
}

const editService = (row) => {
  ElMessage.info('功能开发中')
}

const deleteService = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此服务吗？', '提示', { type: 'warning' })
    ElMessage.success('删除成功')
  } catch (e) {}
}

onMounted(() => {
  loadCategories()
  loadServices()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>