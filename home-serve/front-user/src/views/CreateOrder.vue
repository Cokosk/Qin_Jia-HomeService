<template>
  <div class="create-order">
    <van-nav-bar title="预约下单" left-arrow @click-left="goBack" />

    <van-form @submit="handleSubmit">
      <!-- 服务信息 -->
      <van-cell-group inset title="服务信息">
        <van-cell :title="service?.name" :value="'¥' + service?.price" />
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
        />
        <van-popup v-model:show="showDatePicker" position="bottom">
          <van-date-picker
            v-model="selectedDate"
            title="选择日期"
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
          提交订单
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { serviceApi } from '../api/service'
import { orderApi } from '../api/order'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const service = ref(null)
const showDatePicker = ref(false)
const selectedDate = ref(['2026', '03', '09'])

const form = ref({
  appointmentTime: '',
  address: '',
  remark: ''
})

const loadService = async () => {
  const res = await serviceApi.getDetail(route.params.serviceId)
  if (res.code === 200) {
    service.value = res.data
  }
}

const onDateConfirm = ({ selectedValues }) => {
  form.value.appointmentTime = selectedValues.join('-')
  showDatePicker.value = false
}

const handleSubmit = async () => {
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
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 80px;
}

.submit-btn {
  padding: 20px;
}
</style>