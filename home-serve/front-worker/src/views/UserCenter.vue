<template>
  <div class="user-center">
    <van-nav-bar title="个人中心" />

    <!-- 用户信息 -->
    <div class="user-info">
      <van-image round width="60" height="60" :src="userInfo?.avatar || '/default-avatar.png'" />
      <div class="user-name">{{ userInfo?.nickname || '未登录' }}</div>
      <div class="user-rating" v-if="userInfo?.rating">
        <van-rate v-model="userInfo.rating" readonly />
      </div>
    </div>

    <!-- 收入统计 -->
    <van-cell-group inset title="收入统计">
      <van-cell title="本月收入" :value="'¥' + (income.monthly || 0)" />
      <van-cell title="累计收入" :value="'¥' + (income.total || 0)" />
    </van-cell-group>

    <!-- 功能菜单 -->
    <van-cell-group inset>
      <van-cell title="我的订单" is-link to="/orders" />
      <van-cell title="联系客服" is-link />
      <van-cell title="关于我们" is-link />
    </van-cell-group>

    <van-cell-group inset>
      <van-cell title="退出登录" @click="handleLogout" />
    </van-cell-group>

    <van-tabbar v-model="activeTab" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="shopping-cart-o" to="/grab">抢单</van-tabbar-item>
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
const income = ref({
  monthly: 0,
  total: 0
})

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
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
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.user-name {
  margin-top: 10px;
  font-size: 18px;
}

.user-rating {
  margin-top: 10px;
}

.van-cell-group {
  margin: 10px;
}
</style>