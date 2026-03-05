import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截
request.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

// 响应拦截
request.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

// 订单API
export const orderApi = {
  list: (params) => request.get('/order/list', { params }),
  detail: (id) => request.get('/order/detail', { params: { orderId: id } }),
  cancel: (id) => request.post('/order/cancel', null, { params: { orderId: id } }),
  grabList: () => request.get('/order/grab-pool')
}

// 服务API
export const serviceApi = {
  categoryList: () => request.get('/service/category'),
  list: (params) => request.get('/service/list', { params }),
  detail: (id) => request.get('/service/detail', { params: { serviceId: id } }),
  hot: () => request.get('/service/hot'),
  clearCache: (params) => request.post('/service/clear-cache', null, { params })
}

// 用户API
export const userApi = {
  list: (params) => request.get('/user/list', { params }),
  info: (id) => request.get('/user/info', { params: { userId: id } })
}

export default request