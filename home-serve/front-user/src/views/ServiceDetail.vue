<template>
  <div class="service-detail">
    <van-nav-bar title="服务详情" left-arrow @click-left="goBack" />

    <div class="detail-content" v-if="service">
      <!-- 服务图片 -->
      <div class="service-image">
        <img :src="service.image || '/default-service.png'" alt="">
      </div>

      <!-- 服务信息 -->
      <div class="service-info">
        <h2>{{ service.name }}</h2>
        <p class="description">{{ service.description }}</p>
        <div class="price">
          <span class="label">价格：</span>
          <span class="amount">¥{{ service.price }}</span>
          <span class="unit">/次</span>
        </div>
        <div class="duration">
          <span class="label">服务时长：</span>
          <span>{{ service.duration }}分钟</span>
        </div>
      </div>

      <!-- 服务详情 -->
      <div class="detail-section">
        <h3>服务详情</h3>
        <p>{{ service.detail }}</p>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <van-button type="primary" block @click="goToOrder">
        立即预约
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

const loadService = async () => {
  const res = await serviceApi.getDetail(route.params.id)
  if (res.code === 200) {
    service.value = res.data
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
}

.service-image {
  width: 100%;
  height: 250px;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-info {
  padding: 15px;
  background: #fff;
}

.service-info h2 {
  font-size: 18px;
  margin-bottom: 10px;
}

.description {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.price, .duration {
  margin-bottom: 10px;
}

.price .amount {
  font-size: 20px;
  color: #ff6600;
  font-weight: bold;
}

.detail-section {
  margin-top: 10px;
  padding: 15px;
  background: #fff;
}

.detail-section h3 {
  font-size: 16px;
  margin-bottom: 10px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}
</style>