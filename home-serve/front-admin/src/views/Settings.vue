<template>
  <div class="settings-page">
    <div class="page-header">
      <h2>系统设置</h2>
      <p>管理系统配置与参数</p>
    </div>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基础设置" name="basic">
        <el-form label-width="120px">
          <el-form-item label="系统名称">
            <el-input v-model="settings.systemName" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="settings.contactPhone" />
          </el-form-item>
          <el-form-item label="服务区域">
            <el-input v-model="settings.serviceArea" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="通知设置" name="notification">
        <el-form label-width="120px">
          <el-form-item label="订单通知">
            <el-switch v-model="settings.orderNotify" />
          </el-form-item>
          <el-form-item label="结算提醒">
            <el-switch v-model="settings.settlementNotify" />
          </el-form-item>
          <el-form-item label="系统公告">
            <el-switch v-model="settings.systemNotify" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="安全设置" name="security">
        <el-form label-width="120px">
          <el-form-item label="登录验证">
            <el-switch v-model="settings.loginVerify" />
          </el-form-item>
          <el-form-item label="操作日志">
            <el-switch v-model="settings.operationLog" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')
const settings = reactive({
  systemName: '勤家家政',
  contactPhone: '400-888-8888',
  serviceArea: '杭州市西湖区',
  orderNotify: true,
  settlementNotify: true,
  systemNotify: false,
  loginVerify: true,
  operationLog: true,
})

const saveSettings = () => {
  ElMessage.success('设置已保存')
}
</script>

<style scoped>
.settings-page {
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
</style>
