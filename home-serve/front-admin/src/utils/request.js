import axios from 'axios'

export const createRequest = (options = {}) => {
  const {
    baseURL = '/api',
    timeout = 15000,
    tokenKey = 'admin_token',
    retry = 2,
    retryDelay = 1000
  } = options

  const instance = axios.create({
    baseURL,
    timeout
  })

  instance.interceptors.request.use(
    config => {
      const token = localStorage.getItem(tokenKey)
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    error => Promise.reject(error)
  )

  instance.interceptors.response.use(
    response => response.data,
    error => {
      const originalRequest = error.config
      
      if (error.response) {
        switch (error.response.status) {
          case 401:
            localStorage.removeItem(tokenKey)
            window.location.href = '/login'
            break
          case 403:
            console.error('权限不足')
            break
          case 404:
            console.error('请求资源不存在')
            break
          case 500:
            console.error('服务器错误')
            break
          default:
            console.error('请求失败')
        }
      } else if (error.code === 'ECONNABORTED') {
        console.error('请求超时')
      }
      
      if (!originalRequest._retry && retry > 0 && !originalRequest.url?.includes('/login')) {
        originalRequest._retry = true
        return new Promise(resolve => {
          setTimeout(() => {
            resolve(instance(originalRequest))
          }, retryDelay)
        })
      }
      
      return Promise.reject(error)
    }
  )

  return instance
}

export const token = {
  get: (key) => localStorage.getItem(key),
  set: (key, value) => localStorage.setItem(key, value),
  remove: (key) => localStorage.removeItem(key),
  clear: () => {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('adminId')
  }
}