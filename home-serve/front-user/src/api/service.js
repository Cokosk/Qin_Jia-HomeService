import request from './request'

// 服务相关API
export const serviceApi = {
  // 获取服务分类
  getCategory() {
    return request({
      url: '/service/category',
      method: 'get'
    })
  },
  
  // 获取热门服务
  getHot() {
    return request({
      url: '/service/hot',
      method: 'get'
    })
  },
  
  // 获取服务列表
  getList(params) {
    return request({
      url: '/service/list',
      method: 'get',
      params
    })
  },
  
  // 获取服务详情
  getDetail(id) {
    return request({
      url: '/service/detail',
      method: 'get',
      params: { id }
    })
  }
}