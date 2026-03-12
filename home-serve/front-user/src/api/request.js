import axios from 'axios'
import { showToast, showLoadingToast, closeToast } from 'vant'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求计数器（用于loading控制）
let requestCount = 0

// 显示loading
const showLoading = () => {
  if (requestCount === 0) {
    showLoadingToast({
      message: '加载中...',
      forbidClick: true,
      duration: 0
    })
  }
  requestCount++
}

// 隐藏loading
const hideLoading = () => {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    closeToast()
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 显示loading（可配置跳过）
    if (config.showLoading !== false) {
      showLoading()
    }
    
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    
    // 业务错误处理
    if (res.code && res.code !== 200) {
      // 显示错误提示
      if (response.config.showError !== false) {
        showToast({
          message: res.message || '请求失败',
          position: 'top'
        })
      }
      
      // 401 未授权，跳转登录
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    hideLoading()
    
    // 网络错误处理
    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    }
    
    if (error.config?.showError !== false) {
      showToast({
        message,
        position: 'top'
      })
    }
    
    return Promise.reject(error)
  }
)

export default request
