<template>
  <div class="home">
    <van-nav-bar title="勤家家政 - 服务者端" />

    <!-- 统计卡片 -->
    <div class="stats">
      <van-row gutter="10">
        <van-col span="8">
          <div class="stat-card">
            <div class="stat-value">{{ stats.todayIncome }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </van-col>
        <van-col span="8">
          <div class="stat-card">
            <div class="stat-value">{{ stats.pendingOrders }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </van-col>
        <van-col span="8">
          <div class="stat-card">
            <div class="stat-value">{{ stats.completedOrders }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </van-col>
      </van-row>
    </div>

    <!-- 快捷入口 -->
    <van-cell-group inset title="快捷入口">
      <van-cell title="抢单池" is-link to="/grab" :badge="grabCount || ''">
        <template #icon>
          <van-icon name="shopping-cart-o" size="20" />
        </template>
      </van-cell>
      <van-cell title="我的订单" is-link to="/orders" />
    </van-cell-group>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="shopping-cart-o" to="/grab">抢单</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '../api/order'

const activeTab = ref(0)
const grabCount = ref(0)
const stats = ref({
  todayIncome: 0,
  pendingOrders: 0,
  completedOrders: 0
})

const loadGrabPool = async () => {
  const res = await orderApi.getGrabPool()
  if (res.code === 200) {
    grabCount.value = res.data?.length || 0
  }
}

onMounted(() => {
  loadGrabPool()
})
</script>

<style scoped>
.home {
  padding-bottom: 60px;
}

.stats {
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card {
  text-align: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  margin-top: 5px;
  opacity: 0.8;
}

.van-cell-group {
  margin: 15px;
}
</style>