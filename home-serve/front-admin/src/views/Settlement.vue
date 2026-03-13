<template>
  <div class="settlement-page">
    <div class="page-header">
      <h2>结算管理</h2>
      <p>工人工资结算与付款管理</p>
    </div>
    
    <div class="action-bar">
      <el-button type="primary" @click="batchSettle">批量结算</el-button>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
    </div>
    
    <el-table :data="settlements" stripe>
      <el-table-column prop="workerName" label="工人姓名" width="120" />
      <el-table-column prop="period" label="结算周期" width="180" />
      <el-table-column prop="orderCount" label="订单数" width="100" />
      <el-table-column prop="totalAmount" label="结算金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '已结算' ? 'success' : 'warning'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button v-if="row.status === '待结算'" size="small" type="primary" @click="settle(row)">结算</el-button>
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const dateRange = ref([])
const settlements = ref([
  { workerName: '张师傅', period: '2026-03-01 ~ 2026-03-15', orderCount: 23, totalAmount: 4580, status: '已结算' },
  { workerName: '李师傅', period: '2026-03-01 ~ 2026-03-15', orderCount: 18, totalAmount: 3620, status: '待结算' },
  { workerName: '王师傅', period: '2026-03-01 ~ 2026-03-15', orderCount: 15, totalAmount: 2950, status: '待结算' },
])

const batchSettle = () => {
  ElMessage.success('批量结算成功')
}

const settle = (row) => {
  row.status = '已结算'
  ElMessage.success(`${row.workerName}结算成功`)
}

const viewDetail = (row) => {
  ElMessage.info(`查看${row.workerName}结算详情`)
}
</script>

<style scoped>
.settlement-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #666;
  margin: 0;
}

.action-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
