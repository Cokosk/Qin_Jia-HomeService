<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo">家政服务平台</div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
      >
        <el-menu-item index="/">
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/services">
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/workers">
          <span>服务者管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容 -->
    <el-container>
      <el-header>
        <div class="header-title">管理后台</div>
        <div class="header-actions">
          <el-button type="primary" @click="refreshCache">刷新缓存</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const activeMenu = computed(() => route.path)

const refreshCache = async () => {
  try {
    await axios.post('/api/service/clear-cache')
    ElMessage.success('缓存刷新成功')
  } catch (e) {
    ElMessage.error('缓存刷新失败')
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4a;
}

.el-menu-vertical {
  border-right: none;
  background-color: #304156;
}

.el-menu-item {
  color: #bfcbd9;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  background-color: #263445 !important;
  color: #409eff !important;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.page-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #303133;
}
</style>