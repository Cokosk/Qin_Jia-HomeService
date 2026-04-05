import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('worker_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('worker_token')
          localStorage.removeItem('workerId')
          window.location.href = '/login'
          break
        case 403:
          console.error('权限不足')
          break
        case 500:
          console.error('服务器错误')
          break
      }
    } else if (error.code === 'ECONNABORTED') {
      console.error('请求超时')
    }
    return Promise.reject(error)
  }
)

const api = {
  user: {
    login: (username, password) => instance.post('/user/login', { username, password }),
    getInfo: (userId) => instance.get('/user/info', { params: { userId } })
  },
  order: {
    getGrabPool: () => instance.get('/order/grab-pool'),
    grab: (orderId, workerId) => instance.post('/order/grab', null, { params: { orderId, workerId } }),
    // 工人订单列表，支持status参数筛选
    getWorkerList: (workerId, status) => {
      const params = { workerId }
      if (status !== null && status !== undefined) {
        params.status = status
      }
      return instance.get('/order/worker-list', { params })
    },
    getList: (userId) => instance.get('/order/list', { params: { userId } }),
    getDetail: (orderId) => instance.get('/order/detail', { params: { orderId } }),
    start: (orderId, workerId) => instance.post('/order/start', null, { params: { orderId, workerId } }),
    finish: (orderId, workerId) => instance.post('/order/finish', null, { params: { orderId, workerId } })
  }
}

export default api
