// API 封装 - uni-app 版本（已补充缺失接口）

// 开发环境使用本机IP，生产环境使用实际域名
// 生产环境使用阿里云服务器地址
const BASE_URL = 'http://101.200.180.182:8080/api'

// 请求封装
function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || uni.getStorageSync('worker_token')
    const fullUrl = BASE_URL + options.url
    
    console.log('发起请求:', fullUrl, options.method || 'GET', options.data)
    
    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: 15000,
      success: (res) => {
        console.log('请求成功:', fullUrl, '状态码:', res.statusCode, '数据:', res.data)
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('worker_token')
          uni.removeStorageSync('userId')
          uni.removeStorageSync('workerId')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录或Token已过期'))
        } else if (res.statusCode >= 400) {
          reject(new Error('请求失败: ' + res.statusCode))
        } else {
          resolve(res.data)
        }
      },
      fail: (err) => {
        console.error('请求失败:', fullUrl, err)
        uni.showToast({ title: '网络错误: ' + (err.errMsg || 'Failed to fetch'), icon: 'none' })
        reject(err)
      }
    })
  })
}

// API 模块
const api = {
  auth: {
    login: (data) =>
      request({ url: '/user/login', method: 'POST', data }),
    register: (data) =>
      request({ url: '/user/register', method: 'POST', data })
  },
  user: {
    login: (data) =>
      request({ url: '/user/login', method: 'POST', data }),
    register: (data) => 
      request({ url: '/user/register', method: 'POST', data }),
    getInfo: (userId) => 
      request({ url: '/user/info', data: { userId } }),
    getFullInfo: (userId) => 
      request({ url: '/user/full-info', data: { userId } }),
    updateInfo: (data) => 
      request({ url: '/user/update-info', method: 'POST', data }),
    changePassword: (data) => 
      request({ url: '/user/change-password', method: 'POST', data }),
    sendCode: (phone) => 
      request({ url: '/user/send-code', method: 'POST', data: { phone } }),
    updatePhone: (data) => 
      request({ url: '/user/update-phone', method: 'POST', data }),
    applyWorker: (userId) => 
      request({ url: '/user/apply-worker', method: 'POST', data: null, params: { userId } }),
    verifyToken: (token) => 
      request({ url: '/user/verify-token', data: { token } })
  },
  
  // 新增 worker 模块（高优先级补充）
  worker: {
    getStats: (workerId) => 
      request({ url: '/worker/stats', data: { workerId } }),
    getRecentOrders: (workerId, limit = 5) => 
      request({ url: '/worker/recent-orders', data: { workerId, limit } }),
    getGrabPool: () => 
      request({ url: '/worker/grab-pool' }),
    getDetail: (workerId) => 
      request({ url: '/worker/detail', data: { workerId } })
  },
  
  service: {
    getCategories: () => request({ url: '/service/category' }),
    getHot: () => request({ url: '/service/hot' }),
    getList: (categoryId) => request({ url: '/service/list', data: { categoryId } }),
    getDetail: (serviceId) => request({ url: '/service/detail', data: { serviceId } }),
    // 新增搜索接口
    search: (keyword, categoryId) => 
      request({ url: '/service/search', data: { keyword, categoryId } })
  },
  
  order: {
    create: (data) => request({ url: '/order/create', method: 'POST', data }),
    getList: (userId, pageNum = 1, pageSize = 10) => 
      request({ url: '/order/list', data: { userId, pageNum, pageSize } }),
    getDetail: (orderId) => request({ url: '/order/detail', data: { orderId } }),
    cancel: (orderId, userId) => 
      request({ url: '/order/cancel', method: 'POST', data: { orderId, userId } }),
    // 工人端接口
    grab: (orderId, workerId) => 
      request({ url: '/order/grab', method: 'POST', data: { orderId, workerId } }),
    getGrabPool: () => request({ url: '/worker/grab-pool' }), // 使用worker模块接口
    getWorkerOrders: (workerId, status, pageNum = 1, pageSize = 10) => 
      request({ url: '/order/worker-list', data: { workerId, status, pageNum, pageSize } }),
    startService: (orderId, workerId) => 
      request({ url: '/order/start', method: 'POST', data: { orderId, workerId } }),
    finishService: (orderId, workerId) => 
      request({ url: '/order/finish', method: 'POST', data: { orderId, workerId } })
  },
  
  review: {
    create: (data) => request({ url: '/review/create', method: 'POST', data }),
    // 新增按订单查询评价列表
    getList: (orderId) => request({ url: '/review/list', data: { orderId } }),
    getByOrder: (orderId) => request({ url: '/review/order', data: { orderId } }),
    getWorkerReviews: (workerId, pageNum = 1, pageSize = 10) => 
      request({ url: '/review/worker', data: { workerId, pageNum, pageSize } }),
    getWorkerStats: (workerId) => 
      request({ url: '/review/worker/stats', data: { workerId } }),
    reply: (reviewId, workerId, reply) => 
      request({ url: '/review/reply', method: 'POST', data: { reviewId, workerId, reply } })
  },
  
  payment: {
    create: (orderId, userId, payMethod) => 
      request({ url: '/payment/create', method: 'POST', data: { orderId, userId, payMethod } }),
    // 新增按订单查询支付状态
    getStatus: (orderId) => request({ url: '/payment/status', data: { orderId } }),
    query: (paymentNo) => request({ url: '/payment/query', data: { paymentNo } }),
    mockPay: (paymentNo, userId) => 
      request({ url: '/payment/mock-pay', method: 'POST', data: { paymentNo, userId } }),
    refund: (paymentNo, userId, reason) => 
      request({ url: '/payment/refund', method: 'POST', data: { paymentNo, userId, reason } })
  }
}

export default api
