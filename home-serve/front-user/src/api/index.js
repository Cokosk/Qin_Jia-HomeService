import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  retry: 3,
  retryDelay: 1000
})

instance.interceptors.request.use(config => {
  const path = window.location.pathname
  let token
  if (path.startsWith('/worker')) {
    token = localStorage.getItem('worker_token')
  } else {
    token = localStorage.getItem('token')
  }
  
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
          const path = window.location.pathname
          if (path.startsWith('/worker')) {
            localStorage.removeItem('worker_token')
            localStorage.removeItem('workerId')
          } else {
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
          }
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
    return Promise.reject(error)
  }
)

// API模块
const api = {
  user: {
    // 登录（支持 loginType 参数）
    login: (username, password, loginType = 'user') => 
      instance.post('/user/login', { username, password, loginType }),
    register: (data) => instance.post('/user/register', data),
    getInfo: (userId) => instance.get('/user/info', { params: { userId } }),
    getFullInfo: (userId) => instance.get('/user/full-info', { params: { userId } }),
    updateInfo: (data) => instance.post('/user/update-info', data),
    changePassword: (data) => instance.post('/user/change-password', data),
    sendCode: (phone) => instance.post('/user/send-code', { phone }),
    updatePhone: (data) => instance.post('/user/update-phone', data),
    applyWorker: (userId) => instance.post('/user/apply-worker', null, { params: { userId } })
  },
  service: {
    getCategories: () => instance.get('/service/category'),
    getHot: () => instance.get('/service/hot'),
    getList: (categoryId) => instance.get('/service/list', { params: { categoryId } }),
    getDetail: (serviceId) => instance.get('/service/detail', { params: { serviceId } })
  },
  order: {
    create: (data) => instance.post('/order/create', data),
    getList: (userId) => instance.get('/order/list', { params: { userId } }),
    getDetail: (orderId) => instance.get('/order/detail', { params: { orderId } }),
    cancel: (orderId) => instance.post('/order/cancel', { orderId }),
    // 工人端接口
    grab: (orderId, workerId) => instance.post('/order/grab', null, { params: { orderId, workerId } }),
    getGrabPool: () => instance.get('/order/grab-pool'),
    getWorkerOrders: (workerId) => instance.get('/order/worker-list', { params: { workerId } }),
    startService: (orderId) => instance.post('/order/start', { orderId }),
    finishService: (orderId, data) => instance.post('/order/finish', { orderId, ...data })
  },
  review: {
    create: (data) => instance.post('/review/create', data),
    getList: (orderId) => instance.get('/review/list', { params: { orderId } })
  },
  payment: {
    create: (orderId) => instance.post('/payment/create', { orderId }),
    getStatus: (orderId) => instance.get('/payment/status', { params: { orderId } })
  }
}

export default api
