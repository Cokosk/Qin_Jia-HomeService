<template>
  <div>
    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 24px;">
      <el-button @click="$router.back()" circle>
        <ArrowLeft :size="16" />
      </el-button>
      <h2 style="font-size: 20px; font-weight: 600;">订单详情</h2>
    </div>

    <div v-if="order" class="card">
      <div style="padding: 16px; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 16px; font-weight: 500;">{{ order.serviceName || '服务订单' }}</span>
        <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
      </div>

      <div style="padding: 16px;">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ order.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
          <el-descriptions-item label="工人ID">{{ order.workerId || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ order.price }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="order.payStatus === 1 ? 'success' : 'warning'" size="small">
              {{ order.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务地址" :span="2">{{ order.address }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ order.appointmentTime || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ formatTime(order.grabTime) || '未接单' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(order.startTime) || '未开始' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatTime(order.finishTime) || '未完成' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div v-else class="card" style="padding: 60px; text-align: center; color: #999;">
      加载中...
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from 'lucide-vue-next'

const route = useRoute()
const order = ref(null)

onMounted(async () => {
  try {
    const res = await axios.get('/api/order/detail', {
      params: { orderId: route.params.id }
    })
    if (res.data.code === 200) {
      order.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  }
})

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
</script>
