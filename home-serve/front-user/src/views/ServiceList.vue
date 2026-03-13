<template>
  <div class="service-list page-bg">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <van-search 
        v-model="searchText" 
        placeholder="搜索服务" 
        shape="round"
        show-action
        @search="handleSearch"
        @cancel="$router.back()"
      >
        <template #left-icon>
          <van-icon name="search" color="var(--color-primary)" />
        </template>
      </van-search>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <van-tabs v-model:active="activeCategory" @change="onCategoryChange" shrink sticky offset-top="54px">
        <van-tab title="全部" :name="0" />
        <van-tab 
          v-for="cat in categories" 
          :key="cat.id" 
          :title="cat.name" 
          :name="cat.id" 
        />
      </van-tabs>
      
      <!-- 排序筛选 -->
      <div class="sort-bar">
        <div 
          class="sort-item" 
          :class="{ active: sortType === 'default' }"
          @click="sortType = 'default'"
        >
          综合
        </div>
        <div 
          class="sort-item" 
          :class="{ active: sortType === 'sales' }"
          @click="sortType = 'sales'"
        >
          销量
          <van-icon :name="sortType === 'sales' ? 'arrow-down' : 'arrow'" size="10" />
        </div>
        <div 
          class="sort-item" 
          :class="{ active: sortType === 'price' }"
          @click="togglePriceSort"
        >
          价格
          <van-icon :name="priceOrder === 'asc' ? 'arrow-up' : 'arrow-down'" size="10" />
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <SkeletonServiceCard v-for="i in 4" :key="i" />
    </div>

    <!-- 服务列表 -->
    <div v-else class="service-container">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" success-text="刷新成功">
        <van-list
          v-model:loading="loadingMore"
          :finished="finished"
          finished-text=""
          @load="loadMore"
        >
          <div 
            class="service-card" 
            v-for="service in filteredServices" 
            :key="service.id" 
            @click="goToDetail(service.id)"
          >
            <div class="service-image">
              <div class="service-icon">{{ getServiceIcon(service.categoryId) }}</div>
              <div class="service-badge" v-if="service.badge">{{ service.badge }}</div>
            </div>
            <div class="service-info">
              <div class="service-name">{{ service.name }}</div>
              <div class="service-desc">{{ service.description || '专业服务，品质保障' }}</div>
              <div class="service-tags" v-if="service.tags && service.tags.length">
                <span class="tag" v-for="tag in service.tags.slice(0, 3)" :key="tag">{{ tag }}</span>
              </div>
              <div class="service-meta">
                <div class="service-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ service.price }}</span>
                  <span class="price-unit">/次</span>
                </div>
                <div class="service-sales">
                  <van-icon name="fire-o" color="var(--color-primary)" />
                  已售{{ service.sales || 100 }}+
                </div>
              </div>
            </div>
          </div>
          
          <EmptyState 
            v-if="filteredServices.length === 0 && !loading" 
            type="search" 
            title="未找到相关服务"
            description="换个关键词试试"
          />
        </van-list>
      </van-pull-refresh>
      
      <div v-if="filteredServices.length > 0 && finished" class="list-end">
        <span>— 已加载全部 —</span>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" active-color="var(--color-primary)" inactive-color="#999">
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/services">服务</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { serviceApi } from '../api/service'
import { showToast } from 'vant'
import SkeletonServiceCard from '../components/SkeletonServiceCard.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const route = useRoute()

const activeTab = ref(1)
const activeCategory = ref(0)
const categories = ref([])
const services = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const searchText = ref('')
const sortType = ref('default')
const priceOrder = ref('asc')
const page = ref(1)

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

// 筛选和排序后的服务列表
const filteredServices = computed(() => {
  let result = [...services.value]
  
  // 搜索筛选
  if (searchText.value.trim()) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(s => 
      s.name.toLowerCase().includes(keyword) || 
      (s.description && s.description.toLowerCase().includes(keyword))
    )
  }
  
  // 分类筛选
  if (activeCategory.value) {
    result = result.filter(s => s.categoryId === activeCategory.value)
  }
  
  // 排序
  if (sortType.value === 'sales') {
    result.sort((a, b) => (b.sales || 0) - (a.sales || 0))
  } else if (sortType.value === 'price') {
    result.sort((a, b) => priceOrder.value === 'asc' ? a.price - b.price : b.price - a.price)
  }
  
  return result
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await serviceApi.getCategory({ showLoading: false })
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
    const res = await serviceApi.getList({ showLoading: false })
    if (res.code === 200) {
      services.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error('加载服务列表失败:', e)
    services.value = []
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  setTimeout(() => {
    loadingMore.value = false
    finished.value = true
  }, 500)
}

// 下拉刷新
const onRefresh = async () => {
  await loadServices()
  refreshing.value = false
  showToast('刷新成功')
}

// 分类切换
const onCategoryChange = () => {
  // 重新筛选
}

// 搜索
const handleSearch = () => {
  // 搜索已通过计算属性自动处理
}

// 价格排序切换
const togglePriceSort = () => {
  if (sortType.value !== 'price') {
    sortType.value = 'price'
    priceOrder.value = 'asc'
  } else {
    priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
  }
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
  if (route.query.keyword) {
    searchText.value = route.query.keyword
  }
  await loadServices()
})
</script>

<style scoped>
.service-list {
  padding-bottom: 70px;
  min-height: 100vh;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-soft);
}

.filter-section {
  background: var(--color-bg-card);
}

.sort-bar {
  display: flex;
  padding: 8px 16px;
  gap: 24px;
  border-top: 1px solid #f5f5f5;
}

.sort-item {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.sort-item.active {
  color: var(--color-primary);
  font-weight: 500;
}

.loading-container {
  padding: 8px 12px;
}

.service-container {
  padding: 8px 12px;
}

.service-card {
  display: flex;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  margin-bottom: 10px;
  overflow: hidden;
  box-shadow: var(--shadow-soft);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.service-card:active {
  transform: scale(0.98);
}

.service-image {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #FFE5E5 0%, #FFE8DC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
}

.service-icon {
  font-size: 40px;
}

.service-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 6px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  font-size: 10px;
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.service-info {
  flex: 1;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.service-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.service-tags {
  display: flex;
  gap: 4px;
  margin: 4px 0;
}

.tag {
  font-size: 10px;
  padding: 2px 6px;
  background: #FFF5F5;
  color: var(--color-primary);
  border-radius: var(--radius-sm);
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
  color: var(--color-primary);
  font-weight: 600;
}

.price-value {
  font-size: 18px;
  color: var(--color-primary);
  font-weight: 700;
}

.price-unit {
  font-size: 10px;
  color: var(--color-text-muted);
  margin-left: 2px;
}

.service-sales {
  font-size: 11px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 2px;
}

.list-end {
  text-align: center;
  padding: 20px;
  color: var(--color-text-muted);
  font-size: 12px;
}
</style>
