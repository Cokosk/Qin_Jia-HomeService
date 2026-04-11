import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/dashboard/Index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('../views/users/List.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/categories/List.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/services',
    name: 'Services',
    component: () => import('../views/services/List.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/orders/List.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/orders/Detail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reviews',
    name: 'Reviews',
    component: () => import('../views/reviews/List.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
