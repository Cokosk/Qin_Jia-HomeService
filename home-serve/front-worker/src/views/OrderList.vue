<template>
  <div class="order-list">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="activeStatus" @change="onStatusChange">
      <van-tab title="全部" name="" />
      <van-tab title="待处理" name="pending" />
      <van-tab title="进行中" name="processing" />
      <van-tab title="已完成" name="completed" />
    </van-tabs>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="loadOrders"
    >
      <van-cell-group inset v-for="order in orders" :key="order.id">
        <van-cell :title="order.serviceName" :value="getStatusText(order.status)" is-link @click="goToDetail(order.id)">
          <template #label>
            <div>预约时间：{{ order.appointmentTime }}</div>
            <div>地址：{{ order.address }}</div>
            <div class="price">¥{{ order.price }}</div>
          </template>
        </van-cell>
      </van-cell-group>
    </van-list>

    <van-tabbar v-model="activeTab" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="shopping-cart-o" to="/grab">抢单</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '../api/order'

const router = useRouter()

const activeTab = ref(2)
const activeStatus = ref('')
const orders = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getList({
      page: page.value,
      size: 10,
      status: activeStatus.value
    })
    if (res.code === 200) {
      orders.value = [...orders.value, ...res.data.records]
      page.value++
      finished.value = !res.data.hasMore
    }
  } finally {
    loading.value = false
  }
}

const onStatusChange = () => {
  orders.value = []
  page.value = 1
  finished.value = false
  loadOrders()
}

const goToDetail = (id) => {
  router.push(`/order/${id}`)
}
</script>

<style scoped>
.order-list {
  padding-bottom: 60px;
}

.van-cell-group {
  margin: 10px;
}

.price {
  color: #ff6600;
  font-weight: bold;
  margin-top: 5px;
}
</style>