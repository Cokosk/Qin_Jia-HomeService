<template>
  <div class="order-detail">
    <van-nav-bar title="订单详情" left-arrow @click-left="goBack" />

    <div v-if="order">
      <!-- 订单状态 -->
      <van-cell-group inset title="订单状态">
        <van-cell>
          <template #title>
            <van-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</van-tag>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 服务信息 -->
      <van-cell-group inset title="服务信息">
        <van-cell title="服务名称" :value="order.serviceName" />
        <van-cell title="服务价格" :value="'¥' + order.price" />
      </van-cell-group>

      <!-- 预约信息 -->
      <van-cell-group inset title="预约信息">
        <van-cell title="预约时间" :value="order.appointmentTime" />
        <van-cell title="服务地址" :value="order.address" />
        <van-cell title="联系电话" :value="order.customerPhone" />
        <van-cell title="备注" :value="order.remark || '无'" />
      </van-cell-group>

      <!-- 订单信息 -->
      <van-cell-group inset title="订单信息">
        <van-cell title="订单编号" :value="order.orderNo" />
        <van-cell title="创建时间" :value="order.createTime" />
      </van-cell-group>

      <!-- 操作按钮 -->
      <div class="actions">
        <van-button v-if="order.status === 'pending'" type="primary" block @click="handleStart">
          开始服务
        </van-button>
        <van-button v-if="order.status === 'processing'" type="success" block @click="handleFinish">
          完成服务
        </van-button>
        <van-button v-if="order.status === 'completed'" type="default" block disabled>
          服务已完成
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../api/order'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const loadOrder = async () => {
  const res = await orderApi.getDetail(route.params.id)
  if (res.code === 200) {
    order.value = res.data
  }
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success',
    cancelled: 'default'
  }
  return typeMap[status] || 'default'
}

const handleStart = async () => {
  const res = await orderApi.start(route.params.id)
  if (res.code === 200) {
    showToast('已开始服务')
    loadOrder()
  }
}

const handleFinish = async () => {
  const res = await orderApi.finish(route.params.id)
  if (res.code === 200) {
    showToast('服务已完成')
    loadOrder()
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.van-cell-group {
  margin: 10px;
}

.actions {
  padding: 20px;
}
</style>