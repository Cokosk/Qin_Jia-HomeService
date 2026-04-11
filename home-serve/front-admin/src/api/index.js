import { createRequest, token } from '../utils/request'

const instance = createRequest({ tokenKey: 'admin_token' })

instance.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          token.clear()
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

const api = {
  admin: {
    getStats: () => instance.get('/admin/stats'),
    getUserList: (params) => instance.get('/admin/user/list', { params }),
    getUserDetail: (userId) => instance.get('/admin/user/detail', { params: { userId } }),
    updateUserStatus: (userId, status) => instance.post('/admin/user/status', null, { params: { userId, status } }),
    updateUserRole: (userId, role) => instance.post('/admin/user/role', null, { params: { userId, role } }),
    adjustCredit: (userId, creditScore) => instance.post('/admin/user/credit', null, { params: { userId, creditScore } }),
    
    getServiceList: (params) => instance.get('/admin/service/list', { params }),
    addService: (data) => instance.post('/admin/service/add', data),
    updateService: (data) => instance.post('/admin/service/update', data),
    updateServiceStatus: (serviceId, status) => instance.post('/admin/service/status', null, { params: { serviceId, status } }),
    deleteService: (serviceId) => instance.post('/admin/service/delete', null, { params: { serviceId } }),
    
    getCategoryList: () => instance.get('/admin/category/list'),
    addCategory: (data) => instance.post('/admin/category/add', data),
    updateCategory: (data) => instance.post('/admin/category/update', data),
    deleteCategory: (categoryId) => instance.post('/admin/category/delete', null, { params: { categoryId } }),
    updateCategoryStatus: (categoryId, status) => instance.post('/admin/category/status', null, { params: { categoryId, status } }),
    
    getOrderList: (params) => instance.get('/admin/order/list', { params }),
    cancelOrder: (orderId) => instance.post('/admin/order/cancel', null, { params: { orderId } }),
    
    getReviewList: (params) => instance.get('/admin/review/list', { params }),
    updateReviewStatus: (reviewId, status) => instance.post('/admin/review/status', null, { params: { reviewId, status } })
  },
  service: {
    getCategories: () => instance.get('/service/category')
  }
}

export default api