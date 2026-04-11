<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <Shield :size="48" style="color: #FF6B00;" />
        </div>
        <h1 class="login-title">勤家管理后台</h1>
        <p class="login-subtitle">家政服务平台管理系统</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入管理员账号" 
            prefix-icon="User" 
            size="large" 
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            size="large" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn" 
            @click="handleLogin" 
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>仅限管理员访问 | 安全登录系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Shield } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    router.replace('/')
  }
})

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await axios.post('/api/user/login', {
      username: form.username,
      password: form.password
    })
    
    if (res.data.code === 200) {
      // 检查是否是管理员
      if (res.data.role !== 2) {
        ElMessage.error('请使用管理员账号登录')
        return
      }
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_userId', res.data.userId)
      ElMessage.success('登录成功')
      
      // 跳转到之前访问的页面或首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #FFF5EB 0%, #FFE8D6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.login-footer p {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>
