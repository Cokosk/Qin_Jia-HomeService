<template>
  <div class="order-detail page-bg">
    <van-nav-bar title="订单详情" left-arrow @click-left="goBack" />

    <div v-if="order" class="detail-content">
      <!-- 订单状态卡片 -->
      <div class="status-card" :class="'status-' + order.status">
        <div class="status-icon">
          <van-icon :name="getStatusIcon(order.status)" size="32" />
        </div>
        <div class="status-info">
          <div class="status-title">{{ getStatusText(order.status) }}</div>
          <div class="status-desc">{{ getStatusDesc(order.status) }}</div>
        </div>
      </div>

      <!-- 订单进度时间线 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="clock-o" color="var(--color-primary)" />
          <span>订单进度</span>
        </div>
        <div class="timeline">
          <div 
            class="timeline-item" 
            v-for="(step, index) in orderSteps" 
            :key="index"
            :class="{ active: step.active, done: step.done }"
          >
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-title">{{ step.title }}</div>
              <div class="timeline-time">{{ step.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 服务人员信息 -->
      <div class="section-card" v-if="order.workerName">
        <div class="section-title">
          <van-icon name="user-o" color="var(--color-primary)" />
          <span>服务人员</span>
        </div>
        <div class="worker-info">
          <div class="worker-avatar">{{ order.workerName[0] }}</div>
          <div class="worker-detail">
            <div class="worker-name">{{ order.workerName }}</div>
            <div class="worker-meta">
              <van-icon name="star" color="#FFD700" />
              <span>{{ order.workerRating || '5.0' }}</span>
              <span class="divider">|</span>
              <span>服务{{ order.workerOrders || 100 }}单</span>
            </div>
          </div>
          <div class="worker-actions">
            <van-button size="small" type="primary" plain round @click="contactWorker">
              <van-icon name="phone-o" /> 联系
            </van-button>
          </div>
        </div>
      </div>

      <!-- 服务信息 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="description" color="var(--color-primary)" />
          <span>服务信息</span>
        </div>
        <div class="info-list">
          <div class="info-item">
            <div class="info-label">服务名称</div>
            <div class="info-value">{{ order.serviceName }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">预约时间</div>
            <div class="info-value">{{ order.appointmentTime || '待确认' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">服务地址</div>
            <div class="info-value">{{ order.address }}</div>
          </div>
          <div class="info-item" v-if="order.remark">
            <div class="info-label">备注</div>
            <div class="info-value">{{ order.remark }}</div>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="orders-o" color="var(--color-primary)" />
          <span>订单信息</span>
        </div>
        <div class="info-list">
          <div class="info-item">
            <div class="info-label">订单编号</div>
            <div class="info-value copy-btn" @click="copyOrderNo">
              {{ order.orderNo }}
              <van-icon name="description" color="#999" />
            </div>
          </div>
          <div class="info-item">
            <div class="info-label">创建时间</div>
            <div class="info-value">{{ order.createTime }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">订单金额</div>
            <div class="info-value price">¥{{ order.price }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-else class="loading-container">
      <van-skeleton title :row="8" />
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar" v-if="order">
      <template v-if="order.status === 'pending'">
        <van-button type="default" round @click="handleCancel">取消订单</van-button>
        <van-button type="primary" round @click="editOrder">修改预约</van-button>
      </template>
      <template v-else-if="order.status === 'processing'">
        <van-button type="primary" round block @click="contactWorker">联系服务者</van-button>
      </template>
      <template v-else-if="order.status === 'completed'">
        <van-button type="default" round @click="reorder">再来一单</van-button>
        <van-button type="primary" round @click="goToReview">去评价</van-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../api/order'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const statusConfig = {
  pending: { icon: 'clock-o', text: '待处理', desc: '正在为您匹配服务人员，请耐心等待' },
  processing: { icon: 'logistics', text: '进行中', desc: '服务人员已接单，正在前往服务地点' },
  completed: { icon: 'checked', text: '已完成', desc: '服务已完成，感谢您的信任' },
  cancelled: { icon: 'cross', text: '已取消', desc: '订单已取消' }
}

const getStatusIcon = (status) => statusConfig[status]?.icon || 'clock-o'
const getStatusText = (status) => statusConfig[status]?.text || status
const getStatusDesc = (status) => statusConfig[status]?.desc || ''

// 订单进度
const orderSteps = computed(() => {
  if (!order.value) return []
  const steps = [
    { title: '提交订单', time: order.value.createTime, done: true, active: false },
    { title: '订单确认', time: order.value.confirmTime || '-', done: ['processing', 'completed'].includes(order.value.status), active: false },
    { title: '开始服务', time: order.value.startTime || '-', done: order.value.status === 'completed', active: order.value.status === 'processing' },
    { title: '服务完成', time: order.value.finishTime || '-', done: false, active: order.value.status === 'completed' }
  ]
  return steps
})

const loadOrder = async () => {
  try {
    const res = await orderApi.getDetail(route.params.id)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (e) {
    console.error('加载订单详情失败:', e)
  }
}

const goBack = () => router.back()

const copyOrderNo = () => {
  navigator.clipboard?.writeText(order.value.orderNo)
  showToast('已复制订单编号')
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确定要取消此订单吗？'
    })
    const res = await orderApi.cancel(route.params.id)
    if (res.code === 200) {
      showToast('订单已取消')
      loadOrder()
    }
  } catch (e) {
    // 用户取消
  }
}

const editOrder = () => showToast('修改预约功能开发中')

const contactWorker = () => showToast('正在呼叫服务者...')

const reorder = () => router.push(`/service/${order.value.serviceId}`)

const goToReview = () => showToast('评价功能开发中')

onMounted(() => loadOrder())
</script>

<style scoped>
.order-detail {
  background: var(--color-bg-base);
  min-height: 100vh;
  padding-bottom: 80px;
}

.detail-content {
  padding: 12px;
}

/* 状态卡片 */
.status-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--radius-lg);
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.status-card.status-pending {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFEDD5 100%);
}

.status-card.status-processing {
  background: linear-gradient(135deg, #E6F7FF 0%, #D6F4FF 100%);
}

.status-card.status-completed {
  background: linear-gradient(135deg, #F6FFED 0%, #E8FFD9 100%);
}

.status-card.status-cancelled {
  background: linear-gradient(135deg, #FFF1F0 0%, #FFE7E6 100%);
}

.status-icon {
  width: 56px;
  height: 56px;
  background: rgba(255,255,255,0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-card.status-pending .status-icon { color: #FA8C16; }
.status-card.status-processing .status-icon { color: #1890FF; }
.status-card.status-completed .status-icon { color: #52C41A; }
.status-card.status-cancelled .status-icon { color: #FF4D4F; }

.status-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.status-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* section卡片 */
.section-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 14px;
}

/* 时间线 */
.timeline {
  padding-left: 8px;
}

.timeline-item {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  position: relative;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: #eee;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-item.done::before {
  background: #52C41A;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #eee;
  flex-shrink: 0;
  margin-top: 4px;
}

.timeline-item.done .timeline-dot {
  background: #52C41A;
}

.timeline-item.active .timeline-dot {
  background: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(255,107,53,0.2);
}

.timeline-title {
  font-size: 14px;
  color: var(--color-text-primary);
}

.timeline-time {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

/* 服务人员 */
.worker-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.worker-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 500;
}

.worker-detail {
  flex: 1;
}

.worker-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.worker-meta {
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}

.divider {
  margin: 0 4px;
}

/* 信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.info-label {
  font-size: 13px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--color-text-primary);
  text-align: right;
  word-break: break-all;
}

.info-value.price {
  font-size: 18px;
  color: var(--color-primary);
  font-weight: 700;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 16px;
  background: var(--color-bg-card);
  box-shadow: 0 -2px 12px rgba(0,0,0,0.08);
  display: flex;
  gap: 12px;
}

.bottom-bar .van-button {
  flex: 1;
}

@media (min-width: 768px) {
  .bottom-bar {
    max-width: 500px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
