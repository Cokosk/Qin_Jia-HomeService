<template>
  <div>
    <div class="page-card">
      <div class="page-title">数据概览</div>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="今日订单" :value="stats.todayOrders">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="待抢单" :value="stats.pendingOrders">
            <template #prefix>
              <el-icon><Clock /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="服务者在线" :value="stats.workersOnline">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="今日收入" :value="stats.todayIncome">
            <template #prefix>
              <el-icon><Money /></el-icon>
            </template>
          </el-statistic>
        </el-col>
      </el-row>
    </div>

    <div class="page-card" style="margin-top: 20px;">
      <div class="page-title">热门服务</div>
      <el-table :data="hotServices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="服务名称" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="120" />
      </el-table>
    </div>

    <div class="page-card" style="margin-top: 20px;">
      <div class="page-title">最新订单</div>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="serviceName" label="服务" />
        <el-table-column prop="price" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Clock, User, Money } from '@element-plus/icons-vue'
import { serviceApi, orderApi } from '../api'

const stats = ref({
  todayOrders: 0,
  pendingOrders: 0,
  workersOnline: 0,
  todayIncome: 0
})

const hotServices = ref([])
const recentOrders = ref([])

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待抢单', 1: '已接单', 2: '服务中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

onMounted(async () => {
  try {
    // 获取热门服务
    const hotRes = await serviceApi.hot()
    if (hotRes.code === 200) {
      hotServices.value = hotRes.data || []
    }
    
    // 获取抢单池
    const poolRes = await orderApi.grabList()
    if (poolRes.code === 200) {
      stats.value.pendingOrders = poolRes.data?.length || 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>