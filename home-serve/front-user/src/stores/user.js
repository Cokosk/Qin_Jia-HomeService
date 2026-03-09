import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // 登录
  const login = async (data) => {
    const res = await userApi.login(data)
    if (res.code === 200) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    }
    throw new Error(res.message)
  }

  // 注册
  const register = async (data) => {
    const res = await userApi.register(data)
    if (res.code === 200) {
      return res
    }
    throw new Error(res.message)
  }

  // 获取用户信息
  const getUserInfo = async () => {
    if (!token.value) return
    const res = await userApi.getInfo()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    token,
    login,
    register,
    getUserInfo,
    logout
  }
})