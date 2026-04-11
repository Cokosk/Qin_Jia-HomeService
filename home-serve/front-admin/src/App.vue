<template>
  <!-- 登录页面独立显示 -->
  <div v-if="isLoginPage" class="login-page">
    <router-view />
  </div>
  
  <!-- 主页面带侧边栏 -->
  <div v-else class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <span style="font-size: 18px; font-weight: 600; color: #FF6B00;">勤家管理后台</span>
      </div>
      <div style="display: flex; align-items: center; gap: 16px;">
        <span style="font-size: 14px; color: #666;">管理员</span>
        <el-button type="primary" size="small" @click="logout">退出登录</el-button>
      </div>
    </header>
    
    <div style="display: flex; flex: 1;">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <router-link to="/" class="sidebar-item" :class="{ active: $route.path === '/' }">
            <LayoutDashboard :size="18" />
            <span style="margin-left: 8px;">数据看板</span>
          </router-link>
          <router-link to="/users" class="sidebar-item" :class="{ active: $route.path.startsWith('/users') }">
            <Users :size="18" />
            <span style="margin-left: 8px;">用户管理</span>
          </router-link>
          <router-link to="/services" class="sidebar-item" :class="{ active: $route.path.startsWith('/services') }">
            <Package :size="18" />
            <span style="margin-left: 8px;">服务管理</span>
          </router-link>
          <router-link to="/orders" class="sidebar-item" :class="{ active: $route.path.startsWith('/orders') }">
            <ClipboardList :size="18" />
            <span style="margin-left: 8px;">订单管理</span>
          </router-link>
          <router-link to="/reviews" class="sidebar-item" :class="{ active: $route.path.startsWith('/reviews') }">
            <Star :size="18" />
            <span style="margin-left: 8px;">评价管理</span>
          </router-link>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, Users, Package, ClipboardList, Star } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const isLoginPage = computed(() => route.path === '/login')

function logout() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_userId')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
}

.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar {
  width: 200px;
  background: #001529;
  min-height: calc(100vh - 56px);
}

.sidebar-nav {
  padding: 16px 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  transition: all 0.3s;
}

.sidebar-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-item.active {
  color: #fff;
  background: #FF6B00;
}

.admin-content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
