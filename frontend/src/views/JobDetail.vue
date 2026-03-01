<template>
  <div class="job-detail-page">
    <div class="container" v-loading="loading">
      <template v-if="job">
        <div class="main-content">
          <!-- 职位信息卡片 -->
          <div class="job-card card">
            <div class="job-header">
              <div class="job-main">
                <h1 class="job-title">{{ job.title }}</h1>
                <div class="job-salary salary">{{ formatSalary(job.salaryMin, job.salaryMax) }}</div>
              </div>
              <div class="job-meta">
                <span><el-icon><Location /></el-icon>{{ job.city }}</span>
                <span><el-icon><Suitcase /></el-icon>{{ job.experience }}</span>
                <span><el-icon><School /></el-icon>{{ job.education }}</span>
              </div>
              <div class="job-tags">
                <span v-for="tag in (job.benefits || [])" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>
            
            <div class="job-actions">
              <el-button 
                type="primary" 
                size="large"
                :loading="applying"
                @click="handleApplyOrChat"
              >
                {{ job.hasApplied ? '继续沟通' : '立即沟通' }}
              </el-button>
              <el-button 
                size="large" 
                :type="isFavorited ? 'warning' : 'default'"
                @click="toggleFavorite"
                :loading="favoriteLoading"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
          
          <!-- 职位描述 -->
          <div class="card">
            <h2 class="section-title">职位描述</h2>
            <div class="job-description">
              <template v-if="job.responsibility">
                <h3>岗位职责</h3>
                <pre class="content">{{ job.responsibility }}</pre>
              </template>
              <template v-if="job.requirement">
                <h3>任职要求</h3>
                <pre class="content">{{ job.requirement }}</pre>
              </template>
              <template v-if="job.description && !job.responsibility && !job.requirement">
                <pre class="content">{{ job.description }}</pre>
              </template>
            </div>
            
            <div class="job-keywords" v-if="job.keywords && job.keywords.length">
              <h3>职位关键词</h3>
              <div class="keywords">
                <span v-for="kw in job.keywords" :key="kw" class="tag">{{ kw }}</span>
              </div>
            </div>
          </div>
          
          <!-- 工作地点 -->
          <div class="card location-card">
            <h2 class="section-title">工作地点</h2>
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ job.address || job.city }}</span>
            </div>
            <!-- 地图展示区 -->
            <div class="map-container">
               <iframe 
                 class="map-iframe"
                 :src="'https://map.baidu.com/?newmap=1&ie=utf-8&s=s%26wd%3D' + encodeURIComponent(job.companyName + ' ' + (job.address || job.city))"
                 frameborder="0"
               ></iframe>
               <div class="map-overlay" @click="openExternalMap">
                 <el-button type="primary" size="small">查看完整地图</el-button>
               </div>
            </div>
          </div>
        </div>
        
        <!-- 侧边栏 -->
        <div class="sidebar">
          <!-- 公司信息 -->
          <div class="card company-card" @click="goToCompany">
            <div class="company-header">
              <img :src="job.companyLogo || defaultLogo" class="company-logo" alt="" />
              <div class="company-info">
                <h3 class="company-name">{{ job.companyName }}</h3>
                <div class="company-meta">
                  {{ job.industry }} · {{ job.companyScale }}
                </div>
              </div>
            </div>
            <p class="company-desc">{{ job.companyDescription }}</p>
            <el-button text type="primary">查看公司详情 →</el-button>
          </div>
          
          <!-- 发布者信息 -->
          <div class="card publisher-card">
            <div class="publisher-header">
              <el-avatar :size="48" :src="job.publisherAvatar">
                {{ job.publisherName?.charAt(0) }}
              </el-avatar>
              <div class="publisher-info">
                <div class="publisher-name">{{ job.publisherName }}</div>
                <div class="publisher-title">HR</div>
              </div>
            </div>
            <el-button type="primary" class="chat-btn" :loading="applying" @click="handleApplyOrChat">
              <el-icon><ChatDotRound /></el-icon>
              {{ job.hasApplied ? '继续沟通' : '立即沟通' }}
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { formatSalary } from '@/utils'
import { useUserStore } from '@/stores/user'
import { Location, Star, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const job = ref(null)
const isFavorited = ref(false)
const favoriteLoading = ref(false)
const applying = ref(false)

const defaultLogo = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 60 60"><rect fill="%2300bebd" width="60" height="60" rx="8"/><text x="30" y="38" text-anchor="middle" fill="white" font-size="20">Co</text></svg>'

async function loadJob() {
  loading.value = true
  try {
    const res = await api.get(`/jobs/${route.params.id}`)
    if (res.code === 200) {
      job.value = res.data
      // 检查是否已收藏
      if (userStore.isLoggedIn && !userStore.isRecruiter) {
        checkFavoriteStatus()
      }
    }
  } catch (error) {
    console.error('Failed to load job:', error)
    ElMessage.error('职位不存在')
    router.push('/jobs')
  } finally {
    loading.value = false
  }
}

async function checkFavoriteStatus() {
  try {
    const res = await api.get(`/favorites/check/${route.params.id}`)
    if (res.code === 200) {
      isFavorited.value = res.data
    }
  } catch (error) {
    console.error('Failed to check favorite:', error)
  }
}

async function handleApply() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.isRecruiter) {
    ElMessage.warning('招聘者无法投递简历')
    return
  }
  
  try {
    // 先投递简历
    const applyRes = await api.post(`/applications/apply/${job.value.id}`)
    if (applyRes.code === 200) {
      job.value.hasApplied = true
      
      // 发起沟通：向职位发布者发送初始消息
      const chatRes = await api.post('/messages/start-conversation', {
        receiverId: job.value.publisherId,
        content: `您好，我对贵公司的「${job.value.title}」职位很感兴趣，希望能进一步沟通！`
      })
      
      if (chatRes.code === 200) {
        ElMessage.success('投递成功，已发起沟通')
        // 跳转到聊天页面
        router.push(`/messages/${job.value.publisherId}`)
      } else {
        ElMessage.success('投递成功')
      }
    } else {
      ElMessage.error(applyRes.message)
    }
  } catch (error) {
    console.error('Failed to apply:', error)
  }
}

function handleApplyOrChat() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.isRecruiter) {
    ElMessage.warning('招聘者无法投递简历')
    return
  }
  
  // 如果已投递，直接跳转到聊天页面
  if (job.value.hasApplied) {
    router.push(`/messages/${job.value.publisherId}`)
    return
  }
  
  // 否则进行投递
  applying.value = true
  handleApply().finally(() => {
    applying.value = false
  })
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.isRecruiter) {
    ElMessage.warning('招聘者无法收藏职位')
    return
  }
  
  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      // 取消收藏
      await api.delete(`/favorites/${route.params.id}`)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await api.post(`/favorites/${route.params.id}`)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

function goToCompany() {
  router.push(`/companies/${job.value.companyId}`)
}

function openExternalMap() {
  const query = job.value.companyName + ' ' + (job.value.address || job.value.city)
  window.open(`https://map.baidu.com/search/${encodeURIComponent(query)}`, '_blank')
}

onMounted(() => {
  loadJob()
})
</script>

<style lang="scss" scoped>
.job-detail-page {
  padding: 20px 0;
  background: #f5f6f7;
  min-height: calc(100vh - 60px);
  
  .container {
    display: flex;
    gap: 20px;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
}

.main-content {
  flex: 1;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.job-card {
  .job-header {
    margin-bottom: 20px;
  }
  
  .job-main {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
  }
  
  .job-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }
  
  .job-salary {
    font-size: 24px;
  }
  
  .job-meta {
    display: flex;
    gap: 24px;
    color: #666;
    margin-bottom: 16px;
    
    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
  
  .job-actions {
    display: flex;
    gap: 12px;
    padding-top: 20px;
    border-top: 1px solid #e8e8e8;
    
    .el-button {
      padding: 12px 32px;
    }
  }
}

.job-description {
  h3 {
    font-size: 15px;
    font-weight: 600;
    margin: 20px 0 12px;
    color: #333;
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  .content {
    font-family: inherit;
    white-space: pre-wrap;
    line-height: 1.8;
    color: #666;
  }
}

.job-keywords {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
  
  h3 {
    font-size: 15px;
    margin-bottom: 12px;
  }
  
  .keywords {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.location-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 16px;
}

.map-container {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  
  .map-iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
  
  .map-overlay {
    position: absolute;
    bottom: 12px;
    right: 12px;
    z-index: 10;
  }
}

.company-card {
  cursor: pointer;
  
  .company-header {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .company-logo {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    object-fit: cover;
  }
  
  .company-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  .company-meta {
    font-size: 13px;
    color: #999;
  }
  
  .company-desc {
    color: #666;
    font-size: 13px;
    line-height: 1.6;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.publisher-card {
  .publisher-header {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .publisher-name {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 4px;
  }
  
  .publisher-title {
    font-size: 13px;
    color: #999;
  }
  
  .chat-btn {
    width: 100%;
  }
}
</style>
