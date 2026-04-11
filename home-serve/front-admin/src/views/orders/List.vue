<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">订单管理</h2>

    <!-- 筛选栏 -->
    <div class="card" style="margin-bottom: 16px;">
      <div style="padding: 16px; display: flex; gap: 16px; align-items: center;">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;">
          <el-option label="待接单" :value="0" />
          <el-option label="已接单" :value="1" />
          <el-option label="进行中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-input v-model="userIdFilter" placeholder="用户ID" type="number" style="width: 120px;" />
        <el-input v-model="workerIdFilter" placeholder="工人ID" type="number" style="width: 120px;" />
        <el-button type="primary" @click="loadOrders">搜索</el-button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="card">
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column label="服务" width="100">
          <template #default="{ row }">{{ row.serviceName || '服务订单' }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="workerId" label="工人ID" width="80">
          <template #default="{ row }">{{ row.workerId || '未分配' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="80">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button size="small" type="danger" @click="cancelOrder(row)" 
                       v-if="row.status <= 1">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="pageNum = $event; loadOrders()"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const statusFilter = ref(null)
const userIdFilter = ref('')
const workerIdFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadOrders())

async function loadOrders() {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    if (userIdFilter.value) params.userId = userIdFilter.value
    if (workerIdFilter.value) params.workerId = workerIdFilter.value
    
    const res = await api.admin.getOrderList(params)
    if (res.code === 200) {
      orders.value = res.data || []
      total.value = res.total || 0
    }
  } catch (e) {
    console.error(e)
  }
}

function statusText(status) {
  const map = { 0: '待接单', 1: '已接单', 2: '进行中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

function statusType(status) {
  const map = { 0: 'warning', 1: 'info', 2: 'primary', 3: 'success', 4: 'info' }
  return map[status] || ''
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    const res = await api.admin.cancelOrder(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message)
      row.status = 4
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>
