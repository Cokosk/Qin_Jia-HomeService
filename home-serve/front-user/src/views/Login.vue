<template>
  <div class="login">
    <div class="login-header">
      <h2>勤家家政</h2>
      <p>让服务更简单</p>
    </div>

    <van-form @submit="handleLogin">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-btn">
        <van-button round block type="primary" native-type="submit">
          登录
        </van-button>
      </div>

      <div class="register-link">
        还没有账号？<router-link to="/login?mode=register">立即注册</router-link>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await userStore.login(form.value)
    showToast('登录成功')
    router.push(route.query.redirect || '/')
  } catch (error) {
    showToast(error.message || '登录失败')
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-header {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  opacity: 0.8;
}

.login-btn {
  margin: 20px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-link a {
  color: #1989fa;
}
</style>