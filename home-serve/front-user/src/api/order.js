import request from './request'

// 订单相关API
export const orderApi = {
  // 创建订单
  create(data) {
    return request({
      url: '/order/create',
      method: 'post',
      data
    })
  },
  
  // 获取订单列表
  getList(params) {
    return request({
      url: '/order/list',
      method: 'get',
      params
    })
  },
  
  // 获取订单详情
  getDetail(id) {
    return request({
      url: '/order/detail',
      method: 'get',
      params: { id }
    })
  },
  
  // 取消订单
  cancel(id) {
    return request({
      url: '/order/cancel',
      method: 'post',
      data: { orderId: id }
    })
  }
}