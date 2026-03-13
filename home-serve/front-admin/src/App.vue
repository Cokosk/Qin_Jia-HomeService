<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <i class="ti ti-home"></i>
        </div>
        <span>勤家管理后台</span>
      </div>
      
      <div class="nav-section">
        <div class="nav-section-title">概览</div>
        <router-link to="/" class="nav-item" :class="{ active: activeMenu === '/' }">
          <i class="ti ti-dashboard"></i>
          <span>数据看板</span>
        </router-link>
        <router-link to="/analytics" class="nav-item" :class="{ active: activeMenu === '/analytics' }">
          <i class="ti ti-chart-line"></i>
          <span>数据分析</span>
        </router-link>
      </div>
      
      <div class="nav-section">
        <div class="nav-section-title">业务管理</div>
        <router-link to="/orders" class="nav-item" :class="{ active: activeMenu === '/orders' }">
          <i class="ti ti-file-text"></i>
          <span>订单管理</span>
        </router-link>
        <router-link to="/users" class="nav-item" :class="{ active: activeMenu === '/users' }">
          <i class="ti ti-users"></i>
          <span>用户管理</span>
        </router-link>
        <router-link to="/workers" class="nav-item" :class="{ active: activeMenu === '/workers' }">
          <i class="ti ti-user-check"></i>
          <span>工人管理</span>
        </router-link>
        <router-link to="/services" class="nav-item" :class="{ active: activeMenu === '/services' }">
          <i class="ti ti-package"></i>
          <span>服务管理</span>
        </router-link>
      </div>
      
      <div class="nav-section">
        <div class="nav-section-title">财务</div>
        <router-link to="/finance" class="nav-item" :class="{ active: activeMenu === '/finance' }">
          <i class="ti ti-receipt"></i>
          <span>财务报表</span>
        </router-link>
        <router-link to="/settlement" class="nav-item" :class="{ active: activeMenu === '/settlement' }">
          <i class="ti ti-wallet"></i>
          <span>结算管理</span>
        </router-link>
      </div>
      
      <div class="nav-section">
        <div class="nav-section-title">系统</div>
        <router-link to="/settings" class="nav-item" :class="{ active: activeMenu === '/settings' }">
          <i class="ti ti-settings"></i>
          <span>系统设置</span>
        </router-link>
        <router-link to="/logs" class="nav-item" :class="{ active: activeMenu === '/logs' }">
          <i class="ti ti-history"></i>
          <span>操作日志</span>
        </router-link>
      </div>
    </el-aside>
    
    <!-- 主内容 -->
    <el-container>
      <el-header class="header">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="header-actions">
          <div class="search-box">
            <i class="ti ti-search"></i>
            <input type="text" placeholder="搜索订单、用户...">
          </div>
          <i class="ti ti-bell notification-icon"></i>
          <div class="avatar">管</div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import './style.css'

const route = useRoute()
const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/': '数据看板',
    '/orders': '订单管理',
    '/users': '用户管理',
    '/workers': '工人管理',
    '/services': '服务管理'
  }
  return titles[route.path] || '管理后台'
})
</script>

<style>
/* 引入 Tabler Icons */
@import url('https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@latest/tabler-icons.min.css');

.app-container {
  height: 100vh;
  font-family: var(--font-body);
}

.sidebar {
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: var(--space-xl);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--color-text-primary);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.nav-section {
  margin-bottom: var(--space-lg);
}

.nav-section-title {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-muted);
  margin-bottom: var(--space-sm);
  padding-left: var(--space-sm);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  margin-bottom: 2px;
  transition: all var(--duration-fast) ease;
  cursor: pointer;
}

.nav-item i {
  font-size: 20px;
}

.nav-item:hover {
  background: var(--color-bg-elevated);
  color: var(--color-text-primary);
}

.nav-item.active {
  background: var(--color-primary-soft);
  color: var(--color-primary-light);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--space-lg);
  height: 64px !important;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: var(--space-md);
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--space-sm) var(--space-md);
  gap: var(--space-sm);
}

.search-box i {
  color: var(--color-text-muted);
  font-size: 18px;
}

.search-box input {
  background: transparent;
  border: none;
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
  width: 200px;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
}

.notification-icon {
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.notification-icon:hover {
  color: var(--color-text-primary);
}

.avatar {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  cursor: pointer;
}

.el-main {
  padding: var(--space-lg);
}
</style>
