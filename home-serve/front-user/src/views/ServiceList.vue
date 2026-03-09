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

    <!-- 服务列表 -->
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="loadServices"
    >
      <div class="service-item" v-for="service in services" :key="service.id">
        <van-card
          :title="service.name"
          :desc="service.description"
          :price="service.price"
          :thumb="service.image || '/default-service.png'"
          @click="goToDetail(service.id)"
        />
      </div>
    </van-list>

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
const finished = ref(false)
const page = ref(1)

// 加载分类
const loadCategories = async () => {
  const res = await serviceApi.getCategory()
  if (res.code === 200) {
    categories.value = res.data
  }
}

// 加载服务列表
const loadServices = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10
    }
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
    }
    
    const res = await serviceApi.getList(params)
    if (res.code === 200) {
      services.value = [...services.value, ...res.data.records]
      page.value++
      finished.value = !res.data.hasMore
    }
  } finally {
    loading.value = false
  }
}

// 分类切换
const onCategoryChange = () => {
  services.value = []
  page.value = 1
  finished.value = false
  loadServices()
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/service/${id}`)
}

onMounted(() => {
  loadCategories()
  if (route.query.categoryId) {
    activeCategory.value = Number(route.query.categoryId)
  }
})
</script>

<style scoped>
.service-list {
  padding-bottom: 60px;
}

.service-item {
  margin: 10px;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
</style>