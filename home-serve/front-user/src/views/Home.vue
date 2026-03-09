<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <van-search v-model="searchText" placeholder="搜索服务" />
    </div>

    <!-- 服务分类 -->
    <div class="category-section">
      <div class="section-title">服务分类</div>
      <div class="category-list">
        <div 
          class="category-item" 
          v-for="category in categories" 
          :key="category.id"
          @click="goToServices(category.id)"
        >
          <div class="category-icon">{{ category.icon }}</div>
          <div class="category-name">{{ category.name }}</div>
        </div>
      </div>
    </div>

    <!-- 热门服务 -->
    <div class="hot-section">
      <div class="section-title">热门服务</div>
      <div class="service-list">
        <div 
          class="service-card" 
          v-for="service in hotServices" 
          :key="service.id"
          @click="goToServiceDetail(service.id)"
        >
          <div class="service-image">
            <img :src="service.image || '/default-service.png'" alt="">
          </div>
          <div class="service-info">
            <div class="service-name">{{ service.name }}</div>
            <div class="service-price">¥{{ service.price }}/次</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab">
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
import { serviceApi } from '../api/service'

const router = useRouter()
const searchText = ref('')
const activeTab = ref(0)
const categories = ref([])
const hotServices = ref([])

// 加载分类
const loadCategories = async () => {
  const res = await serviceApi.getCategory()
  if (res.code === 200) {
    categories.value = res.data
  }
}

// 加载热门服务
const loadHotServices = async () => {
  const res = await serviceApi.getHot()
  if (res.code === 200) {
    hotServices.value = res.data
  }
}

// 跳转到服务列表
const goToServices = (categoryId) => {
  router.push({ path: '/services', query: { categoryId } })
}

// 跳转到服务详情
const goToServiceDetail = (serviceId) => {
  router.push({ path: `/service/${serviceId}` })
}

onMounted(() => {
  loadCategories()
  loadHotServices()
})
</script>

<style scoped>
.home {
  padding-bottom: 60px;
}

.search-bar {
  padding: 10px;
  background: #fff;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  padding: 15px;
  background: #fff;
}

.category-list {
  display: flex;
  overflow-x: auto;
  padding: 10px;
  background: #fff;
}

.category-item {
  flex-shrink: 0;
  width: 80px;
  text-align: center;
  padding: 10px;
}

.category-icon {
  font-size: 30px;
  margin-bottom: 5px;
}

.category-name {
  font-size: 12px;
  color: #333;
}

.service-list {
  padding: 10px;
  background: #f5f5f5;
}

.service-card {
  display: flex;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
}

.service-image {
  width: 100px;
  height: 100px;
}

.service-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-info {
  flex: 1;
  padding: 10px;
}

.service-name {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
}

.service-price {
  font-size: 14px;
  color: #ff6600;
}
</style>