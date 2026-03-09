<template>
  <div class="order-list">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="activeStatus" @change="onStatusChange">
      <van-tab title="全部" name="" />
      <van-tab title="待处理" name="pending" />
      <van-tab title="进行中" name="processing" />
      <van-tab title="已完成" name="completed" />
    </van-tabs>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-container">
      <div class="order-item" v-for="order in orders" :key="order.id" @click="goToDetail(order.id)">
        <div class="order-card">
          <div class="order-header">
            <span class="order-service">{{ order.serviceName }}</span>
            <span class="order-status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</span>
          </div>
          <div class="order-info">
            <div class="info-row">
              <span class="info-label">预约时间</span>
              <span class="info-value">{{ order.appointmentTime || '待确认' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">地址</span>
              <span class="info-value">{{ order.address }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">价格</span>
              <span class="info-price">🦞 ¥{{ order.price }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <van-empty v-if="orders.length === 0" description="暂无订单" />
    </div>

    <van-tabbar v-model="activeTab" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/services">服务</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '../api/order'

const router = useRouter()

const activeTab = ref(2)
const activeStatus = ref('')
const orders = ref([])
const loading = ref(false)

const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeStatus.value) {
      params.status = activeStatus.value
    }
    
    const res = await orderApi.getList(params)
    if (res.code === 200) {
      orders.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    orders.value = []
  } finally {
    loading.value = false
  }
}

const onStatusChange = () => {
  loadOrders()
}

const goToDetail = (id) => {
  router.push(`/order/${id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  padding-bottom: 60px;
  background: #f8f8f8;
  min-height: 100vh;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.order-container {
  padding: 8px 12px;
}

.order-item {
  margin-bottom: 10px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
}

.order-service {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.order-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status-pending {
  background: #FFF7E6;
  color: #FA8C16;
}

.status-processing {
  background: #E6F7FF;
  color: #1890FF;
}

.status-completed {
  background: #F6FFED;
  color: #52C41A;
}

.status-cancelled {
  background: #FFF1F0;
  color: #FF4D4F;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.info-label {
  color: #999;
}

.info-value {
  color: #333;
}

.info-price {
  color: #FF6B35;
  font-weight: 600;
}
</style>