import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/services',
    name: 'ServiceList',
    component: () => import('../views/ServiceList.vue'),
    meta: { title: '服务列表' }
  },
  {
    path: '/service/:id',
    name: 'ServiceDetail',
    component: () => import('../views/ServiceDetail.vue'),
    meta: { title: '服务详情' }
  },
  {
    path: '/order/create/:serviceId',
    name: 'CreateOrder',
    component: () => import('../views/CreateOrder.vue'),
    meta: { title: '预约下单', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('../views/UserCenter.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory('/user/'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '勤家家政'
  
  const token = localStorage.getItem('token')
  
  // 需要登录但未登录
  if (to.meta.requiresAuth && !token) {
    // 保存原目标路径，登录后跳回
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } 
  // 已登录访问登录页
  else if (to.path === '/login' && token) {
    next({ path: '/' })
  } 
  else {
    next()
  }
})

export default router
