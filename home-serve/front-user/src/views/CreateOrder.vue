<template>
  <div class="create-order">
    <van-nav-bar title="预约下单" left-arrow @click-left="goBack" />

    <van-form @submit="handleSubmit">
      <!-- 服务信息 -->
      <van-cell-group inset title="服务信息">
        <van-cell :title="service?.name" :value="'🦞 ¥' + service?.price" />
      </van-cell-group>

      <!-- 预约时间 -->
      <van-cell-group inset title="预约时间">
        <van-field
          v-model="form.appointmentTime"
          is-link
          readonly
          label="选择时间"
          placeholder="请选择预约时间"
          @click="showDatePicker = true"
          :rules="[{ required: true, message: '请选择预约时间' }]"
        />
        <van-popup v-model:show="showDatePicker" position="bottom">
          <van-date-picker
            v-model="selectedDate"
            title="选择日期"
            :min-date="minDate"
            @confirm="onDateConfirm"
            @cancel="showDatePicker = false"
          />
        </van-popup>
      </van-cell-group>

      <!-- 服务地址 -->
      <van-cell-group inset title="服务地址">
        <van-field
          v-model="form.address"
          label="详细地址"
          placeholder="请输入服务地址"
          type="textarea"
          rows="2"
          :rules="[{ required: true, message: '请输入地址' }]"
        />
      </van-cell-group>

      <!-- 备注 -->
      <van-cell-group inset title="备注">
        <van-field
          v-model="form.remark"
          label="备注信息"
          placeholder="请输入备注（选填）"
          type="textarea"
          rows="2"
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit">
          🦞 提交订单
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { serviceApi } from '../api/service'
import { orderApi } from '../api/order'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const service = ref(null)
const showDatePicker = ref(false)

// 获取当前日期
const today = new Date()
const selectedDate = ref([
  today.getFullYear().toString(),
  (today.getMonth() + 1).toString().padStart(2, '0'),
  today.getDate().toString().padStart(2, '0')
])

// 最小日期为今天
const minDate = computed(() => new Date())

const form = ref({
  appointmentTime: '',
  address: '',
  remark: ''
})

const loadService = async () => {
  try {
    const res = await serviceApi.getDetail(route.params.serviceId)
    if (res.code === 200) {
      service.value = res.data
    }
  } catch (e) {
    console.error('加载服务失败:', e)
  }
}

const onDateConfirm = ({ selectedValues }) => {
  form.value.appointmentTime = selectedValues.join('-')
  showDatePicker.value = false
}

const handleSubmit = async () => {
  if (!form.value.appointmentTime) {
    showToast('请选择预约时间')
    return
  }
  if (!form.value.address) {
    showToast('请输入地址')
    return
  }
  
  try {
    const res = await orderApi.create({
      serviceId: route.params.serviceId,
      appointmentTime: form.value.appointmentTime,
      address: form.value.address,
      remark: form.value.remark
    })
    if (res.code === 200) {
      showToast('订单创建成功')
      router.push('/orders')
    }
  } catch (error) {
    showToast(error.message || '创建失败')
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadService()
})
</script>

<style scoped>
.create-order {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 80px;
}

.submit-btn {
  padding: 20px;
}
</style>