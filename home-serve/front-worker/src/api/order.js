import request from './request'

// 订单相关API
export const orderApi = {
  // 获取抢单池
  getGrabPool() {
    return request({
      url: '/order/grab-pool',
      method: 'get'
    })
  },
  
  // 抢单
  grab(orderId) {
    return request({
      url: '/order/grab',
      method: 'post',
      data: { orderId }
    })
  },
  
  // 获取服务者订单列表
  getList(params) {
    return request({
      url: '/order/worker-list',
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
  
  // 开始服务
  start(id) {
    return request({
      url: '/order/start',
      method: 'post',
      data: { orderId: id }
    })
  },
  
  // 完成服务
  finish(id) {
    return request({
      url: '/order/finish',
      method: 'post',
      data: { orderId: id }
    })
  }
}