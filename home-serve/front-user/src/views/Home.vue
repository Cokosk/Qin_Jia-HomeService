<template>
  <div class="home">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <van-search 
        v-model="searchText" 
        placeholder="搜索服务" 
        shape="round"
        @search="handleSearch"
      />
    </div>

    <!-- 轮播图 -->
    <div class="banner-section">
      <van-swipe class="banner-swipe" :autoplay="3000" indicator-color="white">
        <van-swipe-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" :style="{ background: banner.bg }">
            <div class="banner-content">
              <div class="banner-title">{{ banner.title }}</div>
              <div class="banner-desc">{{ banner.desc }}</div>
            </div>
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 服务分类 -->
    <div class="category-section">
      <div class="section-title">
        <span>🦞 服务分类</span>
      </div>
      <div class="category-list">
        <div 
          class="category-item" 
          v-for="category in categories" 
          :key="category.id"
          @click="goToServices(category.id)"
        >
          <div class="category-icon">
            {{ getCategoryEmoji(category.icon) }}
          </div>
          <div class="category-name">{{ category.name }}</div>
        </div>
      </div>
    </div>

    <!-- 热门服务 -->
    <div class="hot-section">
      <div class="section-title">
        <span>🔥 热门服务</span>
        <span class="more" @click="goToServices()">查看全部 ></span>
      </div>
      <div class="service-list">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadMore"
          >
            <div 
              class="service-card" 
              v-for="service in hotServices" 
              :key="service.id"
              @click="goToServiceDetail(service.id)"
            >
              <div class="service-image">
                <div class="service-icon">{{ getServiceIcon(service) }}</div>
              </div>
              <div class="service-info">
                <div class="service-name">{{ service.name }}</div>
                <div class="service-desc">{{ service.description || '专业服务，品质保障' }}</div>
                <div class="service-meta">
                  <div class="service-price">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ service.price }}</span>
                    <span class="price-unit">/次</span>
                  </div>
                  <div class="service-sales">已售{{ service.sales || 100 }}+</div>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" active-color="#FF6B35">
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
import { showToast } from 'vant'

const router = useRouter()
const searchText = ref('')
const activeTab = ref(0)
const categories = ref([])
const hotServices = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

// 轮播图数据
const banners = ref([
  { id: 1, title: '新人专享', desc: '首单立减20元', bg: 'linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)' },
  { id: 2, title: '品质保障', desc: '专业服务人员', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 3, title: '限时特惠', desc: '保洁服务8折起', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }
])

// 分类图标映射
const categoryIconMap = {
  cleaning: { emoji: '🧹', label: '保洁' },
  appliance: { emoji: '🔧', label: '家电' },
  nanny: { emoji: '👶', label: '月嫂' },
  repair: { emoji: '🛠️', label: '维修' },
  move: { emoji: '🚚', label: '搬家' },
  default: { emoji: '📋', label: '服务' }
}

// 获取分类 emoji
const getCategoryEmoji = (icon) => {
  return categoryIconMap[icon]?.emoji || categoryIconMap.default.emoji
}

// 获取服务图标
const getServiceIcon = (service) => {
  return getCategoryEmoji(service.icon) || '🦞'
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await serviceApi.getCategory({ showLoading: false })
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载热门服务
const loadHotServices = async () => {
  try {
    const res = await serviceApi.getHot({ showLoading: false })
    if (res.code === 200) {
      hotServices.value = res.data
    }
  } catch (error) {
    console.error('加载服务失败:', error)
  }
}

// 加载更多
const loadMore = async () => {
  // 模拟分页加载
  loading.value = true
  setTimeout(() => {
    loading.value = false
    finished.value = true
  }, 500)
}

// 下拉刷新
const onRefresh = async () => {
  await Promise.all([loadCategories(), loadHotServices()])
  refreshing.value = false
  showToast('刷新成功')
}

// 搜索
const handleSearch = () => {
  if (searchText.value.trim()) {
    router.push({ path: '/services', query: { keyword: searchText.value } })
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
  background: #f5f5f5;
  min-height: 100vh;
}

.search-bar {
  padding: 10px 12px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.search-bar :deep(.van-search__content) {
  background: rgba(255, 255, 255, 0.95);
}

/* 轮播图 */
.banner-section {
  padding: 10px 12px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.banner-swipe {
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: #fff;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.9;
}

/* 分类 */
.category-section {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  overflow: hidden;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  padding: 14px 12px 10px;
  color: #333;
}

.section-title .more {
  font-size: 12px;
  color: #999;
  font-weight: 400;
}

.category-list {
  display: flex;
  overflow-x: auto;
  padding: 0 8px 14px;
  gap: 4px;
}

.category-list::-webkit-scrollbar {
  display: none;
}

.category-item {
  flex-shrink: 0;
  width: 68px;
  text-align: center;
  padding: 10px 4px;
  border-radius: 10px;
  transition: all 0.2s;
}

.category-item:active {
  background: #f5f5f5;
  transform: scale(0.95);
}

.category-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 6px;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFF0E8 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.1);
}

.category-name {
  font-size: 12px;
  color: #333;
  line-height: 1.3;
}

/* 热门服务 */
.hot-section {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  overflow: hidden;
}

.service-list {
  padding: 0 12px 12px;
}

.service-card {
  display: flex;
  background: #f8f8f8;
  border-radius: 10px;
  margin-bottom: 10px;
  overflow: hidden;
  transition: all 0.2s;
}

.service-card:active {
  transform: scale(0.98);
}

.service-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FFE5E5 0%, #FFE8DC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-icon {
  font-size: 32px;
}

.service-info {
  flex: 1;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.service-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.service-desc {
  font-size: 11px;
  color: #999;
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.service-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 12px;
  color: #FF6B35;
  font-weight: 600;
}

.price-value {
  font-size: 16px;
  color: #FF6B35;
  font-weight: 700;
}

.price-unit {
  font-size: 10px;
  color: #999;
  margin-left: 2px;
}

.service-sales {
  font-size: 10px;
  color: #999;
}
</style>
