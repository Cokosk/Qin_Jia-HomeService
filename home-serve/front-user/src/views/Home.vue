<template>
  <div class="home page-bg">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-top">
        <div class="logo">勤家</div>
        <div class="location">
          <i class="ph ph-map-pin"></i>
          <span>杭州市西湖区</span>
          <i class="ph ph-caret-down"></i>
        </div>
      </div>
      <div class="search-box">
        <i class="ph ph-magnifying-glass"></i>
        <input 
          type="text" 
          v-model="searchText" 
          placeholder="搜索保洁、维修、搬家..."
          @focus="goToSearch"
          @keyup.enter="handleSearch"
        >
      </div>
    </header>

    <!-- 主内容 -->
    <main class="main">
      <!-- 分类导航 -->
      <div class="categories">
        <div 
          class="category-item" 
          v-for="category in displayCategories" 
          :key="category.id"
          @click="goToServices(category.id)"
        >
          <div class="category-icon" :style="getCategoryStyle(category)">
            <i :class="getCategoryIcon(category)"></i>
          </div>
          <span class="category-name">{{ category.name }}</span>
        </div>
      </div>

      <!-- 热门服务 -->
      <div class="section-title">
        <span>热门服务</span>
        <span class="more" @click="goToServices()">
          查看全部 <i class="ph ph-caret-right"></i>
        </span>
      </div>

      <!-- 骨架屏 -->
      <div class="service-grid" v-if="servicesLoading">
        <div class="skeleton-card" v-for="i in 4" :key="i">
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-desc"></div>
            <div class="skeleton-footer"></div>
          </div>
        </div>
      </div>

      <!-- 服务列表 -->
      <van-pull-refresh v-else v-model="refreshing" @refresh="onRefresh" success-text="刷新成功">
        <div class="service-grid">
          <div 
            class="service-card" 
            v-for="service in hotServices" 
            :key="service.id"
            @click="goToServiceDetail(service.id)"
          >
            <div class="service-image">
              <i :class="getServiceIcon(service)"></i>
            </div>
            <div class="service-content">
              <div class="service-title">{{ service.name }}</div>
              <div class="service-desc">{{ service.description || '专业服务，品质保障' }}</div>
              <div class="service-footer">
                <div class="service-price">
                  <span class="price-symbol">¥</span>{{ service.price }}<small>/次</small>
                </div>
                <div class="service-sales">已服务{{ service.sales || 100 }}+单</div>
              </div>
            </div>
          </div>
        </div>
        
        <EmptyState 
          v-if="hotServices.length === 0 && !servicesLoading" 
          type="service" 
          title="暂无热门服务"
          description="下拉刷新试试"
        />
      </van-pull-refresh>
    </main>

    <!-- 底部导航 -->
    <nav class="bottom-nav">
      <a href="#/" class="nav-item active">
        <i class="ph ph-fill ph-house"></i>
        <span>首页</span>
      </a>
      <a href="#/services" class="nav-item">
        <i class="ph ph-list-bullets"></i>
        <span>服务</span>
      </a>
      <a href="#/orders" class="nav-item">
        <i class="ph ph-clipboard-text"></i>
        <span>订单</span>
      </a>
      <a href="#/user" class="nav-item">
        <i class="ph ph-user"></i>
        <span>我的</span>
      </a>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { serviceApi } from '../api/service'
import { showToast } from 'vant'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const searchText = ref('')
const categories = ref([])
const hotServices = ref([])
const refreshing = ref(false)
const servicesLoading = ref(true)

// 显示的分类（最多5个）
const displayCategories = computed(() => {
  const defaults = [
    { id: 'cleaning', name: '保洁', icon: 'sparkle' },
    { id: 'repair', name: '维修', icon: 'wrench' },
    { id: 'move', name: '搬家', icon: 'package' },
    { id: 'wash', name: '清洗', icon: 'bathtub' }
  ]
  return categories.value.length > 0 ? categories.value.slice(0, 5) : [...defaults, { id: 'more', name: '更多', icon: 'dots-three' }]
})

// 分类图标映射
const categoryIconMap = {
  cleaning: 'ph ph-sparkle',
  appliance: 'ph ph-wind',
  repair: 'ph ph-wrench',
  move: 'ph ph-package',
  wash: 'ph ph-bathtub',
  nanny: 'ph ph-baby',
  default: 'ph ph-list'
}

// 分类样式映射
const categoryStyleMap = {
  cleaning: { bg: 'rgba(184, 134, 11, 0.1)', color: 'var(--color-primary)' },
  repair: { bg: 'rgba(74, 124, 89, 0.1)', color: 'var(--color-accent-green)' },
  move: { bg: 'rgba(199, 91, 57, 0.1)', color: 'var(--color-accent-terracotta)' },
  wash: { bg: 'rgba(139, 69, 19, 0.1)', color: 'var(--color-secondary)' },
  default: { bg: 'var(--color-primary-soft)', color: 'var(--color-primary)' }
}

const getCategoryIcon = (category) => {
  return categoryIconMap[category.icon] || categoryIconMap.default
}

const getCategoryStyle = (category) => {
  const style = categoryStyleMap[category.icon] || categoryStyleMap.default
  return {
    background: style.bg,
    color: style.color
  }
}

// 服务图标映射
const getServiceIcon = (service) => {
  const iconClass = categoryIconMap[service.icon] || categoryIconMap.cleaning
  return iconClass + ' service-icon'
}

// 加载分类
const loadCategories = async () => {
  try {
    const res = await serviceApi.getCategory({ showLoading: false })
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载热门服务
const loadHotServices = async () => {
  servicesLoading.value = true
  try {
    const res = await serviceApi.getHot({ showLoading: false })
    if (res.code === 200) {
      hotServices.value = res.data || []
    }
  } catch (error) {
    console.error('加载服务失败:', error)
  } finally {
    servicesLoading.value = false
  }
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

// 跳转搜索页
const goToSearch = () => {
  router.push('/search')
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
/* 顶部导航 */
.header {
  background: var(--color-bg-elevated);
  padding: var(--space-md);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--color-border);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.logo {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: var(--color-primary);
}

.location {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 14px;
  color: var(--color-text-secondary);
}

.location i {
  font-size: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: var(--color-bg-base);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  padding: var(--space-sm) var(--space-md);
  gap: var(--space-sm);
}

.search-box i {
  color: var(--color-text-light);
  font-size: 18px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  color: var(--color-text-primary);
  font-family: var(--font-body);
}

.search-box input::placeholder {
  color: var(--color-text-light);
}

/* 主内容 */
.main {
  padding: var(--space-md);
}

/* 分类导航 */
.categories {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}

.category-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.category-item:active .category-icon {
  transform: scale(0.95);
}

.category-icon i {
  font-size: 28px;
}

.category-name {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-family: var(--font-body);
}

/* 区块标题 */
.section-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 500;
  margin-bottom: var(--space-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-primary);
}

.more {
  color: var(--color-text-muted);
  font-size: 14px;
  font-family: var(--font-body);
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 骨架屏 */
.skeleton-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.skeleton-image {
  height: 100px;
  background: linear-gradient(90deg, var(--color-bg-base) 25%, var(--color-bg-warm) 50%, var(--color-bg-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: var(--space-sm) var(--space-md);
}

.skeleton-title,
.skeleton-desc,
.skeleton-footer {
  height: 14px;
  background: var(--color-bg-base);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-sm);
}

.skeleton-desc {
  width: 70%;
  height: 12px;
}

.skeleton-footer {
  width: 40%;
  margin-bottom: 0;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* 服务卡片 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.service-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all 0.2s ease;
}

.service-card:active {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.1);
}

.service-image {
  height: 100px;
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, rgba(74, 124, 89, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-image i {
  font-size: 40px;
  color: var(--color-primary);
}

.service-content {
  padding: var(--space-sm) var(--space-md);
}

.service-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: var(--space-xs);
  color: var(--color-text-primary);
  font-family: var(--font-body);
}

.service-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: var(--space-sm);
  font-family: var(--font-body);
}

.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-price {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-accent-terracotta);
}

.service-price .price-symbol {
  font-size: 12px;
}

.service-price small {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 400;
}

.service-sales {
  font-size: 10px;
  color: var(--color-text-light);
}

/* 底部导航 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-bg-elevated);
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: space-around;
  padding: var(--space-sm) 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-xs) var(--space-md);
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 10px;
  font-family: var(--font-body);
}

.nav-item i {
  font-size: 24px;
}

.nav-item.active {
  color: var(--color-primary);
}
</style>
