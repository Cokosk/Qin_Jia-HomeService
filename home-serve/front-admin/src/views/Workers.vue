<template>
  <div>
    <div class="page-card">
      <div class="page-title">服务者管理</div>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.nickname" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadWorkers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="workers" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="姓名" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="skill" label="擅长技能" />
        <el-table-column prop="ordersCount" label="已完成订单" width="120" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="income" label="总收入" width="120">
          <template #default="{ row }">¥{{ row.income }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
            <el-button type="success" link v-if="row.status === 0" @click="approve(row)">审核</el-button>
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
        @size-change="loadWorkers"
        @current-change="loadWorkers"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const workers = ref([])
const searchForm = ref({ nickname: '', status: '' })
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 })

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '正常', 2: '禁用' }
  return texts[status] || '未知'
}

const loadWorkers = async () => {
  loading.value = true
  // 模拟数据
  workers.value = [
    { id: 1, nickname: '张师傅', phone: '13900139000', skill: '保洁、家电清洗', ordersCount: 156, rating: 4.8, income: 25800, status: 1 },
    { id: 2, nickname: '李阿姨', phone: '13800138001', skill: '月嫂、保姆', ordersCount: 89, rating: 4.9, income: 35600, status: 1 },
    { id: 3, nickname: '王师傅', phone: '13700137002', skill: '家电维修', ordersCount: 0, rating: 0, income: 0, status: 0 }
  ]
  pagination.value.total = 3
  loading.value = false
}

const resetSearch = () => {
  searchForm.value = { nickname: '', status: '' }
  loadWorkers()
}

const viewDetail = (row) => {
  ElMessage.info('查看功能开发中')
}

const approve = (row) => {
  row.status = 1
  ElMessage.success('审核通过')
}

const toggleStatus = (row) => {
  row.status = row.status === 1 ? 2 : 1
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

onMounted(() => {
  loadWorkers()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>