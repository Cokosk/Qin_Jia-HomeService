import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Orders from '../views/Orders.vue'
import Services from '../views/Services.vue'
import Users from '../views/Users.vue'
import Workers from '../views/Workers.vue'
import Analytics from '../views/Analytics.vue'
import Finance from '../views/Finance.vue'
import Settlement from '../views/Settlement.vue'
import Settings from '../views/Settings.vue'
import Logs from '../views/Logs.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/analytics', name: 'Analytics', component: Analytics },
  { path: '/orders', name: 'Orders', component: Orders },
  { path: '/users', name: 'Users', component: Users },
  { path: '/workers', name: 'Workers', component: Workers },
  { path: '/services', name: 'Services', component: Services },
  { path: '/finance', name: 'Finance', component: Finance },
  { path: '/settlement', name: 'Settlement', component: Settlement },
  { path: '/settings', name: 'Settings', component: Settings },
  { path: '/logs', name: 'Logs', component: Logs }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
