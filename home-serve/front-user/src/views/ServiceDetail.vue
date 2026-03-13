<template>
  <div class="service-detail page-bg">
    <!-- 顶部导航 -->
    <van-nav-bar title="服务详情" left-arrow @click-left="goBack">
      <template #right>
        <van-icon :name="isCollected ? 'star' : 'star-o'" :color="isCollected ? '#FFD700' : '#666'" size="20" @click="toggleCollect" />
      </template>
    </van-nav-bar>

    <div class="detail-content" v-if="service">
      <!-- 服务图片轮播 -->
      <div class="service-gallery">
        <van-swipe class="gallery-swipe" :autoplay="4000" indicator-color="var(--color-primary)">
          <van-swipe-item v-for="(img, index) in serviceImages" :key="index">
            <div class="gallery-item" :style="{ background: img.bg }">
              <div class="gallery-icon">{{ getServiceIcon(service.categoryId) }}</div>
              <div class="gallery-label">{{ img.label }}</div>
            </div>
          </van-swipe-item>
        </van-swipe>
        <div class="gallery-badge">
          <van-icon name="like" color="var(--color-primary)" />
          <span>{{ service.sales || 100 }}+人已预约</span>
        </div>
      </div>

      <!-- 服务信息卡片 -->
      <div class="service-card">
        <div class="service-header">
          <h2 class="service-name">{{ service.name }}</h2>
          <div class="service-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ service.price }}</span>
            <span class="price-unit">/次</span>
          </div>
        </div>
        <p class="service-desc">{{ service.description || '专业服务，品质保障' }}</p>
        <div class="service-tags">
          <span class="tag" v-for="tag in serviceTags" :key="tag">
            <van-icon name="passed" color="#52c41a" /> {{ tag }}
          </span>
        </div>
        <div class="service-meta">
          <div class="meta-item">
            <van-icon name="clock-o" />
            <span>约{{ service.duration || 60 }}分钟</span>
          </div>
          <div class="meta-item">
            <van-icon name="shield-o" />
            <span>品质保障</span>
          </div>
          <div class="meta-item">
            <van-icon name="refund-o" />
            <span>不满意重做</span>
          </div>
        </div>
      </div>

      <!-- 服务流程 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="orders-o" color="var(--color-primary)" />
          <span>服务流程</span>
        </div>
        <div class="process-steps">
          <div class="step" v-for="(step, index) in serviceSteps" :key="index">
            <div class="step-num">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-title">{{ step.title }}</div>
              <div class="step-desc">{{ step.desc }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 服务说明 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="description" color="var(--color-primary)" />
          <span>服务说明</span>
        </div>
        <div class="service-notes">
          <div class="note-item" v-for="(note, index) in serviceNotes" :key="index">
            <van-icon name="info-o" color="#999" />
            <span>{{ note }}</span>
          </div>
        </div>
      </div>

      <!-- 用户评价 -->
      <div class="section-card">
        <div class="section-title">
          <van-icon name="comment-o" color="var(--color-primary)" />
          <span>用户评价</span>
          <span class="more-link" @click="showAllReviews">查看全部</span>
        </div>
        <div class="review-summary">
          <div class="score">
            <span class="score-value">{{ avgScore }}</span>
            <span class="score-label">综合评分</span>
          </div>
          <div class="score-bars">
            <div class="bar-item" v-for="i in 5" :key="i">
              <span class="bar-label">{{ 6 - i }}星</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: scoreBars[5-i] + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="review-list">
          <div class="review-item" v-for="review in reviews.slice(0, 3)" :key="review.id">
            <div class="review-header">
              <div class="review-avatar">{{ review.userName[0] }}</div>
              <div class="review-info">
                <div class="review-name">{{ review.userName }}</div>
                <van-rate v-model="review.score" readonly size="12" color="#FFD700" void-icon="star" void-color="#eee" />
              </div>
              <div class="review-date">{{ review.date }}</div>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-else class="loading-container">
      <van-skeleton title :row="10" />
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <div class="bar-left">
        <div class="bar-item" @click="contactService">
          <van-icon name="service-o" size="20" />
          <span>客服</span>
        </div>
        <div class="bar-item" @click="shareService">
          <van-icon name="share-o" size="20" />
          <span>分享</span>
        </div>
      </div>
      <van-button type="primary" class="order-btn" @click="goToOrder">
        <van-icon name="cart-o" />
        立即预约
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { serviceApi } from '../api/service'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const service = ref(null)
const isCollected = ref(false)

// 分类图标映射
const categoryIconMap = {
  1: '🧹',
  2: '🔧',
  3: '👶',
  4: '🛠️'
}

const getServiceIcon = (categoryId) => {
  return categoryIconMap[categoryId] || '📋'
}

// 服务图片
const serviceImages = computed(() => [
  { label: '服务展示', bg: 'linear-gradient(135deg, #FFE5E5 0%, #FFE8DC 100%)' },
  { label: '专业团队', bg: 'linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%)' },
  { label: '品质保障', bg: 'linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%)' }
])

// 服务标签
const serviceTags = computed(() => ['专业认证', '上门服务', '品质保障'])

// 服务流程
const serviceSteps = [
  { title: '在线预约', desc: '选择服务时间，提交预约' },
  { title: '人员匹配', desc: '系统匹配最优服务人员' },
  { title: '上门服务', desc: '专业人员准时上门服务' },
  { title: '服务验收', desc: '确认服务完成，进行评价' }
]

// 服务说明
const serviceNotes = [
  '请提前24小时预约，以便安排服务人员',
  '服务范围涵盖主城区，偏远地区可能产生额外费用',
  '如需取消预约，请提前2小时联系客服',
  '服务完成后请现场验收，如有问题及时反馈'
]

// 评价相关
const avgScore = ref(4.8)
const scoreBars = ref([85, 10, 3, 1, 1])
const reviews = ref([
  { id: 1, userName: '张**', score: 5, content: '服务很专业，师傅很准时，态度也很好，下次还会预约！', date: '2026-03-10' },
  { id: 2, userName: '李**', score: 5, content: '打扫得很干净，细节处理到位，推荐！', date: '2026-03-08' },
  { id: 3, userName: '王**', score: 4, content: '整体不错，价格合理，服务态度好。', date: '2026-03-05' }
])

const loadService = async () => {
  try {
    const res = await serviceApi.getDetail(route.params.id)
    if (res.code === 200) {
      service.value = res.data
    }
  } catch (e) {
    console.error('加载服务详情失败:', e)
  }
}

const goBack = () => router.back()

const goToOrder = () => router.push(`/order/create/${route.params.id}`)

const toggleCollect = () => {
  isCollected.value = !isCollected.value
  showToast(isCollected.value ? '收藏成功' : '已取消收藏')
}

const contactService = () => showToast('客服功能开发中')

const shareService = () => showToast('分享功能开发中')

const showAllReviews = () => showToast('评价列表开发中')

onMounted(() => loadService())
</script>

<style scoped>
.service-detail {
  padding-bottom: 70px;
  background: var(--color-bg-base);
  min-height: 100vh;
}

/* 轮播图 */
.service-gallery {
  position: relative;
}

.gallery-swipe {
  height: 220px;
}

.gallery-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.gallery-icon {
  font-size: 80px;
  margin-bottom: 10px;
}

.gallery-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  background: rgba(255,255,255,0.8);
  padding: 4px 12px;
  border-radius: 20px;
}

.gallery-badge {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(255,255,255,0.95);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 服务卡片 */
.service-card {
  background: var(--color-bg-card);
  margin: 12px;
  padding: 16px;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.service-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  flex: 1;
}

.service-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 600;
}

.price-value {
  font-size: 24px;
  color: var(--color-primary);
  font-weight: 700;
}

.price-unit {
  font-size: 12px;
  color: var(--color-text-muted);
}

.service-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.service-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  font-size: 12px;
  padding: 4px 10px;
  background: #F6FFED;
  color: #52c41a;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: 4px;
}

.service-meta {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.meta-item {
  font-size: 12px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 通用section */
.section-card {
  background: var(--color-bg-card);
  margin: 12px;
  padding: 16px;
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 14px;
}

.more-link {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 400;
}

/* 服务流程 */
.process-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step {
  display: flex;
  gap: 12px;
}

.step-num {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.step-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

/* 服务说明 */
.service-notes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.note-item {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.5;
}

/* 评价区域 */
.review-summary {
  display: flex;
  gap: 20px;
  padding: 12px;
  background: #fafafa;
  border-radius: var(--radius-md);
  margin-bottom: 14px;
}

.score {
  text-align: center;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-primary);
}

.score-label {
  font-size: 12px;
  color: var(--color-text-muted);
  display: block;
}

.score-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: center;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-label {
  font-size: 11px;
  color: var(--color-text-muted);
  width: 28px;
}

.bar-track {
  flex: 1;
  height: 4px;
  background: #eee;
  border-radius: 2px;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border-radius: 2px;
}

/* 评价列表 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.review-info {
  flex: 1;
}

.review-name {
  font-size: 13px;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}

.review-date {
  font-size: 11px;
  color: var(--color-text-muted);
}

.review-content {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 16px;
  background: var(--color-bg-card);
  box-shadow: 0 -2px 12px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  gap: 16px;
}

.bar-left {
  display: flex;
  gap: 20px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 10px;
  color: var(--color-text-secondary);
}

.bar-item span {
  margin-top: 2px;
}

.order-btn {
  flex: 1;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  border-radius: 24px;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
}

@media (min-width: 768px) {
  .bottom-bar {
    max-width: 500px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
