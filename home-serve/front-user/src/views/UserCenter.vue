<template>
  <div class="user-center page-bg">
    <!-- 用户信息卡片 -->
    <div class="user-header">
      <div class="header-bg"></div>
      <div class="user-info">
        <div class="avatar-wrapper" @click="goToProfile">
          <van-image round width="70" height="70" :src="userInfo?.avatar || '/default-avatar.png'" />
          <div class="avatar-badge" v-if="userInfo?.level">
            <span>{{ userInfo.level }}</span>
          </div>
        </div>
        <div class="user-name">{{ userInfo?.nickname || '未登录' }}</div>
        <div class="user-phone" v-if="userInfo?.phone">{{ formatPhone(userInfo.phone) }}</div>
        <div class="login-btn" v-if="!userInfo" @click="goToLogin">
          <van-button type="primary" size="small" round>立即登录</van-button>
        </div>
      </div>
    </div>

    <!-- 订单快捷入口 -->
    <div class="order-shortcuts">
      <div class="section-header">
        <span class="section-title">我的订单</span>
        <span class="more" @click="goToOrders">
          全部订单
          <van-icon name="arrow" />
        </span>
      </div>
      <div class="shortcuts-grid">
        <div class="shortcut-item" @click="goToOrders('pending')">
          <div class="shortcut-icon">
            <van-icon name="clock-o" />
            <span class="badge" v-if="orderCounts.pending">{{ orderCounts.pending }}</span>
          </div>
          <span class="shortcut-text">待处理</span>
        </div>
        <div class="shortcut-item" @click="goToOrders('processing')">
          <div class="shortcut-icon">
            <van-icon name="send-gift-o" />
          </div>
          <span class="shortcut-text">进行中</span>
        </div>
        <div class="shortcut-item" @click="goToOrders('completed')">
          <div class="shortcut-icon">
            <van-icon name="success" />
          </div>
          <span class="shortcut-text">已完成</span>
        </div>
        <div class="shortcut-item" @click="goToOrders('cancelled')">
          <div class="shortcut-icon">
            <van-icon name="close" />
          </div>
          <span class="shortcut-text">已取消</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <van-cell-group inset>
        <van-cell title="我的地址" is-link @click="goToAddress">
          <template #icon>
            <div class="cell-icon location">
              <van-icon name="location-o" />
            </div>
          </template>
        </van-cell>
        <van-cell title="我的优惠券" is-link @click="goToCoupons">
          <template #icon>
            <div class="cell-icon coupon">
              <van-icon name="coupon-o" />
            </div>
          </template>
          <template #value>
            <span class="coupon-count" v-if="couponCount">{{ couponCount }}张可用</span>
          </template>
        </van-cell>
        <van-cell title="我的收藏" is-link @click="goToFavorite">
          <template #icon>
            <div class="cell-icon favorite">
              <van-icon name="star-o" />
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset>
        <van-cell title="联系客服" is-link @click="contactService">
          <template #icon>
            <div class="cell-icon service">
              <van-icon name="service-o" />
            </div>
          </template>
        </van-cell>
        <van-cell title="帮助中心" is-link @click="goToHelp">
          <template #icon>
            <div class="cell-icon help">
              <van-icon name="question-o" />
            </div>
          </template>
        </van-cell>
        <van-cell title="关于我们" is-link @click="goToAbout">
          <template #icon>
            <div class="cell-icon about">
              <van-icon name="info-o" />
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset v-if="userInfo">
        <van-cell title="退出登录" @click="handleLogout" title-style="color: #ff4d4f;">
          <template #icon>
            <div class="cell-icon logout">
              <van-icon name="revoke" />
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <van-tabbar v-model="activeTab" route active-color="var(--color-primary)">
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/services">服务</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(3)
const userInfo = computed(() => userStore.userInfo)
const couponCount = ref(3)

const orderCounts = ref({
  pending: 2,
  processing: 1,
  completed: 0,
  cancelled: 0
})

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}

// 跳转个人资料
const goToProfile = () => {
  if (userInfo.value) {
    router.push('/profile')
  } else {
    goToLogin()
  }
}

// 跳转订单
const goToOrders = (status) => {
  router.push({ path: '/orders', query: { status } })
}

// 跳转地址
const goToAddress = () => {
  router.push('/address')
}

// 跳转优惠券
const goToCoupons = () => {
  router.push('/coupons')
}

// 跳转收藏
const goToFavorite = () => {
  router.push('/favorite')
}

// 联系客服
const contactService = () => {
  showToast('正在连接客服...')
}

// 跳转帮助
const goToHelp = () => {
  router.push('/help')
}

// 跳转关于
const goToAbout = () => {
  router.push('/about')
}

// 退出登录
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？'
    })
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped>
.user-center {
  padding-bottom: 60px;
  background: var(--color-bg-base);
  min-height: 100vh;
}

/* 用户信息 */
.user-header {
  position: relative;
  padding-bottom: 20px;
}

.header-bg {
  height: 180px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 0 0 30px 30px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.user-info {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 30px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.avatar-wrapper :deep(.van-image) {
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 2px 8px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
  font-size: 10px;
  border-radius: 10px;
  font-weight: 600;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-phone {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.login-btn {
  margin-top: 12px;
}

/* 订单快捷入口 */
.order-shortcuts {
  background: var(--color-bg-card);
  margin: 0 12px;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 3;
  margin-top: -10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.more {
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 2px;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-radius: 10px;
  transition: background 0.2s;
}

.shortcut-item:active {
  background: var(--color-bg-base);
}

.shortcut-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFF0E8 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--color-primary);
  position: relative;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #FF4D4F;
  color: #fff;
  font-size: 10px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.shortcut-text {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 功能菜单 */
.menu-section {
  margin-top: 12px;
}

.van-cell-group {
  margin: 0 12px 12px;
  border-radius: 14px;
  overflow: hidden;
}

.cell-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  margin-right: 10px;
}

.cell-icon.location {
  background: #E8F4FF;
  color: #1890FF;
}

.cell-icon.coupon {
  background: #FFF7E6;
  color: #FA8C16;
}

.cell-icon.favorite {
  background: #FFF1F0;
  color: #FF4D4F;
}

.cell-icon.service {
  background: #E8FFE8;
  color: #52C41A;
}

.cell-icon.help {
  background: #F0F5FF;
  color: #722ED1;
}

.cell-icon.about {
  background: #F5F5F5;
  color: var(--color-text-secondary);
}

.cell-icon.logout {
  background: #FFF1F0;
  color: #FF4D4F;
}

.coupon-count {
  font-size: 12px;
  color: var(--color-primary);
}
</style>
