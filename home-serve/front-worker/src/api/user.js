import request from './request'

// 用户相关API
export const userApi = {
  // 登录
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    })
  },
  
  // 获取用户信息
  getInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  }
}