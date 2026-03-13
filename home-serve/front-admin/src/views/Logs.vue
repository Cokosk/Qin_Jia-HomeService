<template>
  <div class="logs-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <p>系统操作记录查询</p>
    </div>
    
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索操作内容" style="width: 200px" clearable />
      <el-select v-model="filterType" placeholder="操作类型" clearable style="width: 150px">
        <el-option label="登录" value="login" />
        <el-option label="订单" value="order" />
        <el-option label="用户" value="user" />
        <el-option label="系统" value="system" />
      </el-select>
      <el-date-picker v-model="filterDate" type="date" placeholder="选择日期" />
    </div>
    
    <el-table :data="logs" stripe>
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="操作内容" />
      <el-table-column prop="ip" label="IP地址" width="140" />
    </el-table>
    
    <div class="pagination">
      <el-pagination layout="prev, pager, next" :total="100" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchKeyword = ref('')
const filterType = ref('')
const filterDate = ref(null)

const logs = ref([
  { time: '2026-03-13 16:30:25', operator: '管理员', type: '登录', content: '登录系统', ip: '192.168.1.100' },
  { time: '2026-03-13 16:25:18', operator: '管理员', type: '订单', content: '修改订单状态：订单#1024 已完成', ip: '192.168.1.100' },
  { time: '2026-03-13 16:20:05', operator: '管理员', type: '用户', content: '新增工人：张师傅', ip: '192.168.1.100' },
  { time: '2026-03-13 16:15:42', operator: '管理员', type: '系统', content: '修改系统设置：通知配置', ip: '192.168.1.100' },
])
</script>

<style scoped>
.logs-page {
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

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
