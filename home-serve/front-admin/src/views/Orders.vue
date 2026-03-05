<template>
  <div>
    <div class="page-card">
      <div class="page-title">订单管理</div>
      
      <!-- 搜索筛选 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待抢单" :value="0" />
            <el-option label="已接单" :value="1" />
            <el-option label="服务中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 -->
      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="serviceName" label="服务名称" />
        <el-table-column prop="price" label="金额" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="160" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
            <el-button type="danger" link v-if="row.status === 0" @click="cancelOrder(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadOrders"
        @current-change="loadOrders"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="服务名称">{{ currentOrder.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentOrder.price }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ currentOrder.appointmentTime }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api'

const loading = ref(false)
const orders = ref([])
const searchForm = ref({ orderNo: '', status: '' })
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 })
const detailVisible = ref(false)
const currentOrder = ref(null)

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待抢单', 1: '已接单', 2: '服务中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.list({
      userId: 1, // TODO: 暂时写死
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    if (res.code === 200) {
      orders.value = res.data || []
      pagination.value.total = res.total || 0
    }
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { orderNo: '', status: '' }
  loadOrders()
}

const viewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const cancelOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', { type: 'warning' })
    const res = await orderApi.cancel(row.id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>