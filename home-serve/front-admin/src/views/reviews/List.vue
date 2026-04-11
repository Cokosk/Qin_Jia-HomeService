<template>
  <div>
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px;">评价管理</h2>

    <!-- 筛选栏 -->
    <div class="card" style="margin-bottom: 16px;">
      <div style="padding: 16px; display: flex; gap: 16px; align-items: center;">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;">
          <el-option label="显示" value="1" />
          <el-option label="隐藏" value="0" />
        </el-select>
        <el-select v-model="ratingFilter" placeholder="评分筛选" clearable style="width: 120px;">
          <el-option label="5星" value="5" />
          <el-option label="4星" value="4" />
          <el-option label="3星" value="3" />
          <el-option label="2星" value="2" />
          <el-option label="1星" value="1" />
        </el-select>
        <el-button type="primary" @click="loadReviews">搜索</el-button>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="card">
      <el-table :data="reviews" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="pageNum = $event; loadReviews()"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const reviews = ref([])
const statusFilter = ref(null)
const ratingFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadReviews())

async function loadReviews() {
  try {
    const res = await api.admin.getReviewList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value,
      rating: ratingFilter.value
    })
    if (res.code === 200) {
      reviews.value = res.data
      total.value = res.total
    }
  } catch (e) {
    console.error(e)
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await api.admin.updateReviewStatus(row.id, newStatus)
  if (res.code === 200) {
    ElMessage.success('状态已更新')
    row.status = newStatus
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}
</script>
