<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">数据看板</h2>

    <!-- 统计卡片 -->
    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;">
      <div class="stat-card">
        <div class="stat-title">总用户数</div>
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-change up">↑ 12%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">活跃服务</div>
        <div class="stat-value">{{ stats.activeServices }}</div>
        <div class="stat-change up">↑ 8%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">今日订单</div>
        <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
        <div class="stat-change up">↑ 15%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">完成率</div>
        <div class="stat-value">{{ completionRate }}%</div>
        <div class="stat-change up">↑ 5%</div>
      </div>
    </div>

    <!-- 详细统计 -->
    <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">用户构成</span>
        </div>
        <div style="padding: 16px;">
          <div style="display: flex; justify-content: space-around; text-align: center;">
            <div>
              <div style="font-size: 32px; font-weight: 600; color: #1890FF;">{{ stats.userCount }}</div>
              <div style="font-size: 14px; color: #999; margin-top: 8px;">普通用户</div>
            </div>
            <div>
              <div style="font-size: 32px; font-weight: 600; color: #FF6B00;">{{ stats.workerCount }}</div>
              <div style="font-size: 14px; color: #999; margin-top: 8px;">服务工人</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">订单状态</span>
        </div>
        <div style="padding: 16px;">
          <div style="display: flex; justify-content: space-around; text-align: center;">
            <div>
              <div style="font-size: 32px; font-weight: 600; color: #FAAD14;">{{ stats.pendingOrders }}</div>
              <div style="font-size: 14px; color: #999; margin-top: 8px;">待接单</div>
            </div>
            <div>
              <div style="font-size: 32px; font-weight: 600; color: #1890FF;">{{ stats.inProgressOrders }}</div>
              <div style="font-size: 14px; color: #999; margin-top: 8px;">进行中</div>
            </div>
            <div>
              <div style="font-size: 32px; font-weight: 600; color: #52C41A;">{{ stats.completedOrders }}</div>
              <div style="font-size: 14px; color: #999; margin-top: 8px;">已完成</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../api'

const stats = ref({
  totalUsers: 0,
  userCount: 0,
  workerCount: 0,
  totalServices: 0,
  activeServices: 0,
  totalOrders: 0,
  pendingOrders: 0,
  inProgressOrders: 0,
  completedOrders: 0,
  totalReviews: 0
})

const completionRate = computed(() => {
  if (stats.value.totalOrders === 0) return 0
  return Math.round((stats.value.completedOrders / stats.value.totalOrders) * 100)
})

onMounted(async () => {
  try {
    const res = await api.admin.getStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
})
</script>
