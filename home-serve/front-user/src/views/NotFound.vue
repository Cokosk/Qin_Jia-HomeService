<template>
  <div class="not-found">
    <div class="content">
      <div class="icon">🦞</div>
      <h1 class="title">404</h1>
      <p class="desc">抱歉，您访问的页面不存在</p>
      <van-button type="primary" round @click="goHome">
        返回首页
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFE5E5 0%, #FFF0E8 100%);
  padding: 20px;
}

.content {
  text-align: center;
}

.icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.title {
  font-size: 48px;
  font-weight: 700;
  color: #FF6B35;
  margin: 0 0 12px;
}

.desc {
  font-size: 16px;
  color: #666;
  margin: 0 0 30px;
}

.van-button {
  min-width: 140px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
}
</style>
