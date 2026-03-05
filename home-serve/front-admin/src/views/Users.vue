<template>
  <div>
    <div class="page-card">
      <div class="page-title">用户管理</div>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择" clearable>
            <el-option label="普通用户" :value="0" />
            <el-option label="服务者" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const loading = ref(false)
const users = ref([])
const searchForm = ref({ username: '', role: '' })
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 })

const getRoleType = (role) => {
  const types = { 0: '', 1: 'warning', 2: 'success' }
  return types[role] || ''
}

const getRoleText = (role) => {
  const texts = { 0: '用户', 1: '服务者', 2: '管理员' }
  return texts[role] || '未知'
}

const loadUsers = async () => {
  loading.value = true
  // 模拟数据
  users.value = [
    { id: 1, username: 'test_user', nickname: '测试用户', phone: '13800138000', role: 0, creditScore: 100, status: 1, createTime: '2024-01-01 10:00:00' },
    { id: 2, username: 'test_worker', nickname: '张师傅', phone: '13900139000', role: 1, creditScore: 100, status: 1, createTime: '2024-01-02 10:00:00' }
  ]
  pagination.value.total = 2
  loading.value = false
}

const resetSearch = () => {
  searchForm.value = { username: '', role: '' }
  loadUsers()
}

const viewDetail = (row) => {
  ElMessage.info('详情功能开发中')
}

const toggleStatus = (row) => {
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>