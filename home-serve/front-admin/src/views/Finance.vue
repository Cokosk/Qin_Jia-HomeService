<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>财务报表</h2>
      <p>财务数据统计与分析</p>
    </div>
    
    <div class="summary-cards">
      <div class="summary-card income">
        <i class="ti ti-arrow-up-circle"></i>
        <div class="summary-info">
          <div class="summary-label">总收入</div>
          <div class="summary-value">¥256,789</div>
        </div>
      </div>
      <div class="summary-card expense">
        <i class="ti ti-arrow-down-circle"></i>
        <div class="summary-info">
          <div class="summary-label">总支出</div>
          <div class="summary-value">¥89,456</div>
        </div>
      </div>
      <div class="summary-card profit">
        <i class="ti ti-cash"></i>
        <div class="summary-info">
          <div class="summary-label">净利润</div>
          <div class="summary-value">¥167,333</div>
        </div>
      </div>
    </div>
    
    <div class="finance-table">
      <h3>收支明细</h3>
      <el-table :data="transactions" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.type === '收入' ? 'text-success' : 'text-danger'">
              {{ row.type === '收入' ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const transactions = ref([
  { date: '2026-03-13', type: '收入', description: '保洁服务订单', amount: 258 },
  { date: '2026-03-12', type: '收入', description: '维修服务订单', amount: 450 },
  { date: '2026-03-11', type: '支出', description: '工人工资结算', amount: 3200 },
  { date: '2026-03-10', type: '收入', description: '搬家服务订单', amount: 800 },
])
</script>

<style scoped>
.finance-page {
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

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.summary-card i {
  font-size: 36px;
}

.summary-card.income i { color: #52c41a; }
.summary-card.expense i { color: #ff4d4f; }
.summary-card.profit i { color: #1890ff; }

.summary-label {
  font-size: 14px;
  color: #666;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
}

.finance-table {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.finance-table h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.text-success { color: #52c41a; }
.text-danger { color: #ff4d4f; }
</style>
