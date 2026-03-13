<template>
  <div class="order-list page-bg">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="activeStatus" @change="onStatusChange" sticky shrink>
      <van-tab title="全部" name="" />
      <van-tab title="待处理" name="pending" />
      <van-tab title="进行中" name="processing" />
      <van-tab title="已完成" name="completed" />
    </van-tabs>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <SkeletonLoader type="order-card" v-for="i in 4" :key="i" />
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-container">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" success-text="刷新成功">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text=""
          @load="loadMore"
        >
          <div class="order-item" v-for="order in orders" :key="order.id" @click="goToDetail(order.id)">
            <div class="order-card">
              <div class="order-header">
                <div class="order-service">
                  <span class="service-icon">{{ getServiceIcon(order.serviceId) }}</span>
                  <span class="service-name">{{ order.serviceName }}</span>
                </div>
                <span class="order-status" :class="'status-' + getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <div class="order-body">
                <div class="info-row">
                  <div class="info-icon">
                    <van-icon name="clock-o" />
                  </div>
                  <div class="info-content">
                    <span class="info-label">预约时间</span>
                    <span class="info-value">{{ order.appointmentTime || '待确认' }}</span>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-icon">
                    <van-icon name="location-o" />
                  </div>
                  <div class="info-content">
                    <span class="info-label">服务地址</span>
                    <span class="info-value">{{ order.address }}</span>
                  </div>
                </div>
                <div class="info-row" v-if="order.workerName">
                  <div class="info-icon">
                    <van-icon name="user-o" />
                  </div>
                  <div class="info-content">
                    <span class="info-label">服务人员</span>
                    <span class="info-value">{{ order.workerName }}</span>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <div class="order-price">
                  <span class="price-label">订单金额</span>
                  <span class="price-value">¥{{ order.price }}</span>
                </div>
                <div class="order-actions">
                  <van-button 
                    v-if="order.status === 'pending'" 
                    size="small" 
                    type="danger"
                    plain
                    @click.stop="cancelOrder(order)"
                  >
                    取消订单
                  </van-button>
                  <van-button 
                    v-if="order.status === 'completed'" 
                    size="small" 
                    type="primary"
                    @click.stop="reorder(order)"
                  >
                    再来一单
                  </van-button>
                  <van-button 
                    v-if="order.status === 'processing'" 
                    size="small" 
                    type="primary"
                    plain
                    @click.stop="contactWorker(order)"
                  >
                    联系服务者
                  </van-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <EmptyState 
            v-if="orders.length === 0 && !loading" 
            type="order" 
            title="暂无订单"
            description="去首页看看吧"
            actionText="去逛逛"
            @action="goHome"
          />
        </van-list>
      </van-pull-refresh>
    </div>

    <van-tabbar v-model="activeTab" route active-color="var(--color-primary)">
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
import { showConfirmDialog, showToast } from 'vant'
import SkeletonLoader from '../components/SkeletonLoader.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()

const activeTab = ref(2)
const activeStatus = ref('')
const orders = ref([])
const loading = ref(false)
const listLoading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

// 服务图标映射
const serviceIcons = {
  1: '🧹',
  2: '🔧',
  3: '👶',
  4: '🛠️'
}

const getServiceIcon = (serviceId) => {
  return serviceIcons[serviceId] || '📋'
}

const getStatusClass = (status) => {
  const classes = {
    pending: 'pending',
    processing: 'processing',
    completed: 'completed',
    cancelled: 'cancelled'
  }
  return classes[status] || 'pending'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

// 加载订单
const loadOrders = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    orders.value = []
    finished.value = false
  }
  
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10
    }
    
    if (activeStatus.value) {
      params.status = activeStatus.value
    }
    
    const res = await orderApi.getList(params)
    if (res.code === 200) {
      const data = Array.isArray(res.data) ? res.data : (res.data.records || [])
      orders.value = isRefresh ? data : [...orders.value, ...data]
      page.value++
      finished.value = data.length < 10
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    orders.value = []
  } finally {
    loading.value = false
    listLoading.value = false
  }
}

// 加载更多
const loadMore = () => {
  loadOrders()
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadOrders(true).finally(() => {
    refreshing.value = false
  })
}

// 状态切换
const onStatusChange = () => {
  loadOrders(true)
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确定要取消此订单吗？'
    })
    await orderApi.cancel(order.id)
    showToast('取消成功')
    loadOrders(true)
  } catch (e) {
    if (e !== 'cancel') {
      showToast('取消失败')
    }
  }
}

// 再来一单
const reorder = (order) => {
  router.push(`/service/${order.serviceId}`)
}

// 联系服务者
const contactWorker = (order) => {
  showToast('正在呼叫服务者...')
}

// 跳转首页
const goHome = () => {
  router.push('/')
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/order/${id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  padding-bottom: 70px;
  min-height: 100vh;
}

.loading-container {
  padding: 12px;
}

.order-container {
  padding: 12px;
}

.order-item {
  margin-bottom: 12px;
}

.order-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 14px 12px;
  border-bottom: 1px solid #f5f5f5;
}

.order-service {
  display: flex;
  align-items: center;
  gap: 8px;
}

.service-icon {
  font-size: 20px;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.order-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: var(--radius-md);
  font-weight: 500;
}

.status-pending {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFEDD5 100%);
  color: #FA8C16;
}

.status-processing {
  background: linear-gradient(135deg, #E6F7FF 0%, #D6F4FF 100%);
  color: #1890FF;
}

.status-completed {
  background: linear-gradient(135deg, #F6FFED 0%, #E8FFD9 100%);
  color: #52C41A;
}

.status-cancelled {
  background: linear-gradient(135deg, #FFF1F0 0%, #FFE7E6 100%);
  color: #FF4D4F;
}

.order-body {
  padding: 12px 14px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-icon {
  width: 24px;
  height: 24px;
  background: var(--color-bg-base);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  color: var(--color-text-muted);
}

.info-value {
  font-size: 13px;
  color: var(--color-text-primary);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: var(--color-bg-warm);
}

.order-price {
  display: flex;
  flex-direction: column;
}

.price-label {
  font-size: 11px;
  color: var(--color-text-muted);
}

.price-value {
  font-size: 18px;
  color: var(--color-primary);
  font-weight: 700;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
