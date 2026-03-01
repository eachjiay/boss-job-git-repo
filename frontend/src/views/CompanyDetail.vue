<template>
  <div class="company-detail-page">
    <div class="container" v-loading="loading">
      <template v-if="company">
        <!-- 公司头部 -->
        <div class="company-header card">
          <div class="header-main">
            <img :src="company.logo || defaultLogo" class="company-logo" alt="" />
            <div class="company-info">
              <h1 class="company-name">{{ company.name }}</h1>
              <div class="company-meta">
                <span>{{ company.industry }}</span>
                <span>{{ company.scale }}</span>
                <span>{{ company.financing }}</span>
              </div>
            </div>
          </div>
          <div class="company-stats">
            <div class="stat-item">
              <div class="stat-value">{{ company.jobCount }}</div>
              <div class="stat-label">在招职位</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ company.bossCount }}</div>
              <div class="stat-label">位BOSS</div>
            </div>
          </div>
        </div>
        
        <div class="content-wrapper">
          <div class="main-content">
            <!-- 公司简介 -->
            <div class="card">
              <h2 class="section-title">公司简介</h2>
              <div class="company-intro">
                <p>{{ company.description }}</p>
                <div v-if="company.detail" class="company-detail">
                  {{ company.detail }}
                </div>
              </div>
            </div>
            
            <!-- 公司福利 -->
            <div class="card" v-if="company.benefits && company.benefits.length">
              <h2 class="section-title">公司福利</h2>
              <div class="benefits">
                <span v-for="benefit in company.benefits" :key="benefit" class="tag">
                  {{ benefit }}
                </span>
              </div>
            </div>
            
            <!-- 公司相册 -->
            <div class="card" v-if="company.images && company.images.length">
              <h2 class="section-title">公司相册</h2>
              <div class="company-images">
                <img 
                  v-for="(img, index) in company.images" 
                  :key="index" 
                  :src="img" 
                  class="company-image"
                  alt=""
                />
              </div>
            </div>
            
            <!-- 热门职位 -->
            <div class="card">
              <h2 class="section-title">热招职位</h2>
              <div class="job-list">
                <div 
                  v-for="job in company.hotJobs" 
                  :key="job.id" 
                  class="job-item"
                  @click="goToJob(job.id)"
                >
                  <div class="job-main">
                    <div class="job-title">{{ job.title }}</div>
                    <div class="job-salary salary">{{ formatSalary(job.salaryMin, job.salaryMax) }}</div>
                  </div>
                  <div class="job-meta">
                    <span>{{ job.city }}</span>
                    <span>{{ job.experience }}</span>
                  </div>
                </div>
              </div>
              <div class="view-all">
                <el-button text type="primary" @click="viewAllJobs">
                  查看全部 {{ company.jobCount }} 个职位 →
                </el-button>
              </div>
            </div>
            
            <!-- 公司地址 -->
            <div class="card">
              <h2 class="section-title">公司地址</h2>
              <div class="address-info">
                <el-icon><Location /></el-icon>
                <span>{{ company.address || company.city }}</span>
              </div>
              <!-- 地图展示区 -->
              <div class="map-container">
                 <iframe 
                   class="map-iframe"
                   :src="'https://map.baidu.com/?newmap=1&ie=utf-8&s=s%26wd%3D' + encodeURIComponent(company.name + ' ' + (company.address || company.city))"
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
            <div class="card">
              <h3 class="sidebar-title">工作时间及福利</h3>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>上午9:00 - 下午7:00</span>
              </div>
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>双休、弹性工作</span>
              </div>
            </div>
            
            <div class="card" v-if="company.businessInfo">
              <h3 class="sidebar-title">工商信息</h3>
              <div class="business-info">
                {{ company.businessInfo }}
              </div>
            </div>
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
import { Location, Clock, Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const company = ref(null)

const defaultLogo = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 80"><rect fill="%2300bebd" width="80" height="80" rx="12"/><text x="40" y="50" text-anchor="middle" fill="white" font-size="28">Co</text></svg>'

async function loadCompany() {
  loading.value = true
  try {
    const res = await api.get(`/companies/${route.params.id}`)
    if (res.code === 200) {
      company.value = res.data
    }
  } catch (error) {
    console.error('Failed to load company:', error)
    ElMessage.error('公司不存在')
    router.push('/')
  } finally {
    loading.value = false
  }
}

function goToJob(id) {
  router.push(`/jobs/${id}`)
}

function viewAllJobs() {
  // 跳转到职位列表并按公司筛选
  router.push({
    path: '/jobs',
    query: { companyId: route.params.id }
  })
}

function openExternalMap() {
  const query = company.value.name + ' ' + (company.value.address || company.value.city)
  window.open(`https://map.baidu.com/search/${encodeURIComponent(query)}`, '_blank')
}

onMounted(() => {
  loadCompany()
})
</script>

<style lang="scss" scoped>
.company-detail-page {
  padding: 20px 0;
  background: #f5f6f7;
  min-height: calc(100vh - 60px);
}

.company-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .header-main {
    display: flex;
    gap: 20px;
  }
  
  .company-logo {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    object-fit: cover;
  }
  
  .company-name {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  .company-meta {
    display: flex;
    gap: 16px;
    color: #666;
    
    span {
      position: relative;
      
      &:not(:last-child)::after {
        content: '·';
        position: absolute;
        right: -10px;
      }
    }
  }
  
  .company-stats {
    display: flex;
    gap: 40px;
    
    .stat-item {
      text-align: center;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #00bebd;
    }
    
    .stat-label {
      font-size: 13px;
      color: #999;
    }
  }
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

.main-content {
  flex: 1;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
  
  .sidebar-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #666;
    margin-bottom: 12px;
    
    .el-icon {
      color: #00bebd;
    }
  }
  
  .business-info {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
  }
}

.company-intro {
  line-height: 1.8;
  color: #666;
  
  .company-detail {
    margin-top: 16px;
    white-space: pre-wrap;
  }
}

.benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.company-images {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  
  .company-image {
    width: 100%;
    aspect-ratio: 4/3;
    object-fit: cover;
    border-radius: 8px;
  }
}

.job-list {
  .job-item {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    
    &:hover .job-title {
      color: #00bebd;
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .job-main {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
  }
  
  .job-title {
    font-size: 15px;
    font-weight: 500;
    transition: color 0.2s;
  }
  
  .job-meta {
    display: flex;
    gap: 16px;
    color: #999;
    font-size: 13px;
  }
}

.view-all {
  padding-top: 16px;
  text-align: center;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 16px;
  
  .el-icon {
    color: #00bebd;
  }
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
</style>
