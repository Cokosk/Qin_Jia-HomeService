<template>
  <div class="service-detail">
    <van-nav-bar title="服务详情" left-arrow @click-left="goBack" />

    <div class="detail-content" v-if="service">
      <!-- 服务图标 -->
      <div class="service-image">
        <div class="service-icon">{{ getServiceIcon(service.categoryId) }}</div>
      </div>

      <!-- 服务信息 -->
      <div class="service-info">
        <h2>{{ service.name }}</h2>
        <p class="description">{{ service.description }}</p>
        <div class="info-row">
          <span class="info-label">服务时长</span>
          <span class="info-value">{{ service.duration }}分钟</span>
        </div>
        <div class="info-row">
          <span class="info-label">服务价格</span>
          <span class="info-price">🦞 ¥{{ service.price }}/次</span>
        </div>
      </div>

      <!-- 服务说明 -->
      <div class="detail-section">
        <h3>📋 服务说明</h3>
        <p>专业服务团队，品质保障。如需了解更多详情，请联系客服咨询。</p>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="loading-container">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <van-button type="primary" block @click="goToOrder">
        🦞 立即预约
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { serviceApi } from '../api/service'

const route = useRoute()
const router = useRouter()
const service = ref(null)

// 分类图标映射
const categoryIconMap = {
  1: '🧹',
  2: '🔧',
  3: '👶',
  4: '🛠️'
}

const getServiceIcon = (categoryId) => {
  return categoryIconMap[categoryId] || '📋'
}

const loadService = async () => {
  try {
    const res = await serviceApi.getDetail(route.params.id)
    if (res.code === 200) {
      service.value = res.data
    }
  } catch (e) {
    console.error('加载服务详情失败:', e)
  }
}

const goBack = () => {
  router.back()
}

const goToOrder = () => {
  router.push(`/order/create/${route.params.id}`)
}

onMounted(() => {
  loadService()
})
</script>

<style scoped>
.service-detail {
  padding-bottom: 70px;
  background: #f8f8f8;
  min-height: 100vh;
}

.service-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #FFE5E5 0%, #FFE8DC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-icon {
  font-size: 80px;
}

.service-info {
  padding: 16px;
  background: #fff;
  margin-bottom: 10px;
}

.service-info h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.description {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.6;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.info-price {
  font-size: 18px;
  color: #FF6B35;
  font-weight: 700;
}

.detail-section {
  padding: 16px;
  background: #fff;
}

.detail-section h3 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.detail-section p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

@media (min-width: 768px) {
  .bottom-bar {
    max-width: 500px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>