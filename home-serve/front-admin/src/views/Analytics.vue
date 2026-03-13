<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>数据分析</h2>
      <p>业务数据可视化分析</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">本月订单</div>
        <div class="stat-value">1,234</div>
        <div class="stat-trend up">↑ 12.5%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">本月收入</div>
        <div class="stat-value">¥89,562</div>
        <div class="stat-trend up">↑ 8.3%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">活跃用户</div>
        <div class="stat-value">567</div>
        <div class="stat-trend up">↑ 5.2%</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">服务评分</div>
        <div class="stat-value">4.8</div>
        <div class="stat-trend">稳定</div>
      </div>
    </div>
    
    <div class="chart-section">
      <div class="chart-card">
        <h3>订单趋势</h3>
        <div class="chart-placeholder">
          <i class="ti ti-chart-line"></i>
          <span>订单趋势图表</span>
        </div>
      </div>
      <div class="chart-card">
        <h3>收入分布</h3>
        <div class="chart-placeholder">
          <i class="ti ti-chart-pie"></i>
          <span>收入分布图表</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.analytics-page {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-trend {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.stat-trend.up {
  color: #52c41a;
}

.chart-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

.chart-placeholder i {
  font-size: 48px;
  margin-bottom: 8px;
}
</style>
