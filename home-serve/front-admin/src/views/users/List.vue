<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">用户管理</h2>

    <!-- 搜索筛选 -->
    <div class="card" style="margin-bottom: 16px;">
      <div style="padding: 16px; display: flex; gap: 16px; align-items: center;">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号" style="width: 200px;" />
        <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 120px;">
          <el-option label="普通用户" value="0" />
          <el-option label="服务工人" value="1" />
          <el-option label="管理员" value="2" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;">
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="card">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? '' : row.role === 1 ? 'warning' : 'danger'">
              {{ roleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)" v-if="row.role !== 2">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="pageNum = $event; loadUsers()"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const keyword = ref('')
const roleFilter = ref(null)
const statusFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadUsers())

async function loadUsers() {
  try {
    const res = await api.admin.getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      role: roleFilter.value,
      status: statusFilter.value
    })
    if (res.code === 200) {
      users.value = res.data
      total.value = res.total
    }
  } catch (e) {
    console.error(e)
  }
}

function roleText(role) {
  const map = { 0: '普通用户', 1: '服务工人', 2: '管理员' }
  return map[role] || '未知'
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确认${action}该用户？`, '提示', { type: 'warning' })
    const res = await api.admin.updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(res.message)
      row.status = newStatus
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function showDetail(row) {
  ElMessageBox.alert(
    `用户ID: ${row.id}\n用户名: ${row.username}\n昵称: ${row.nickname}\n手机号: ${row.phone}\n信用分: ${row.creditScore}`,
    '用户详情'
  )
}
</script>
