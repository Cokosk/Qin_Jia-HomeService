<template>
  <div class="service-list">
    <van-nav-bar title="服务列表" />

    <!-- 分类筛选 -->
    <van-tabs v-model:active="activeCategory" @change="onCategoryChange">
      <van-tab title="全部" :name="0" />
      <van-tab 
        v-for="cat in categories" 
        :key="cat.id" 
        :title="cat.name" 
        :name="cat.id" 
      />
    </van-tabs>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 服务列表 -->
    <div v-else class="service-container">
      <div class="service-item" v-for="service in services" :key="service.id" @click="goToDetail(service.id)">
        <div class="service-card">
          <div class="service-icon">{{ getServiceIcon(service.categoryId) }}</div>
          <div class="service-info">
            <div class="service-name">{{ service.name }}</div>
            <div class="service-desc">{{ service.description }}</div>
            <div class="service-price">
              <span class="price-label">🦞</span>
              <span class="price-value">¥{{ service.price }}</span>
              <span class="price-unit">/次</span>
            </div>
          </div>
        </div>
      </div>
      
      <van-empty v-if="services.length === 0" description="暂无服务" />
      
      <div v-if="services.length > 0" class="list-end">
        <span>— 已加载全部 —</span>
      </div>
    </div>

    <!-- 底部导航 -->
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
import { useRouter, useRoute } from 'vue-router'
import { serviceApi } from '../api/service'

const router = useRouter()
const route = useRoute()

const activeTab = ref(1)
const activeCategory = ref(0)
const categories = ref([])
const services = ref([])
const loading = ref(false)

// 分类图标映射
const categoryIconMap = {
  1: '🧹',
  2: '🔧',
  3: '👶',
  4: '🛠️'
}

// 获取服务图标
const getServiceIcon = (categoryId) => {
  return categoryIconMap[categoryId] || '📋'
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await serviceApi.getCategory()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

// 加载服务列表
const loadServices = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
    }
    
    const res = await serviceApi.getList(params)
    if (res.code === 200) {
      const data = Array.isArray(res.data) ? res.data : []
      
      // 根据分类筛选
      if (activeCategory.value) {
        services.value = data.filter(s => s.categoryId === activeCategory.value)
      } else {
        services.value = data
      }
    }
  } catch (e) {
    console.error('加载服务列表失败:', e)
    services.value = []
  } finally {
    loading.value = false
  }
}

// 分类切换
const onCategoryChange = () => {
  loadServices()
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/service/${id}`)
}

onMounted(async () => {
  await loadCategories()
  if (route.query.categoryId) {
    activeCategory.value = Number(route.query.categoryId)
  }
  await loadServices()
})
</script>

<style scoped>
.service-list {
  padding-bottom: 60px;
  background: #f8f8f8;
  min-height: 100vh;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.service-container {
  padding: 8px 0;
}

.service-item {
  padding: 8px 12px;
}

.service-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.service-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #FFE5E5 0%, #FFE8DC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.service-info {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.service-desc {
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}

.service-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-label {
  font-size: 14px;
}

.price-value {
  font-size: 18px;
  color: #FF6B35;
  font-weight: 700;
}

.price-unit {
  font-size: 12px;
  color: #999;
}

.list-end {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 12px;
}
</style>