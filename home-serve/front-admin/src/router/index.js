import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Orders from '../views/Orders.vue'
import Services from '../views/Services.vue'
import Users from '../views/Users.vue'
import Workers from '../views/Workers.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/orders', name: 'Orders', component: Orders },
  { path: '/services', name: 'Services', component: Services },
  { path: '/users', name: 'Users', component: Users },
  { path: '/workers', name: 'Workers', component: Workers }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router