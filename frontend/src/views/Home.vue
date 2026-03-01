<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="container">
        <h1>找工作，直接谈</h1>
        <p class="subtitle">开启直聊，让求职更高效</p>
        
        <div class="search-box hero-search">
          <el-select v-model="searchForm.city" placeholder="城市" style="width: 120px;">
            <el-option v-for="city in cityList" :key="city" :label="city" :value="city" />
          </el-select>
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索职位、公司"
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch">
            搜索
          </el-button>
        </div>
        
        <!-- 热门搜索 -->
        <div class="hot-keywords">
          <span class="label">热门搜索：</span>
          <span 
            v-for="keyword in hotKeywords" 
            :key="keyword" 
            class="keyword"
            @click="searchByKeyword(keyword)"
          >
            {{ keyword }}
          </span>
        </div>
      </div>
    </section>
    
    <!-- 热门职位 -->
    <section class="section hot-jobs">
      <div class="container">
        <h2 class="section-title">
          <el-icon><Promotion /></el-icon>
          热门职位推荐
        </h2>
        
        <div class="job-list" v-loading="loading">
          <div 
            v-for="job in hotJobs" 
            :key="job.id" 
            class="job-card"
            @click="goToJob(job.id)"
          >
            <div class="job-header">
              <div>
                <div class="job-title">{{ job.title }}</div>
                <div class="job-info">
                  <span><el-icon><Location /></el-icon>{{ job.city }}</span>
                  <span>{{ job.experience }}</span>
                  <span>{{ job.education }}</span>
                </div>
              </div>
              <div class="job-salary">{{ formatSalary(job.salaryMin, job.salaryMax) }}</div>
            </div>
            
            <div class="job-tags">
              <span v-for="tag in (job.keywords || []).slice(0, 4)" :key="tag" class="tag">
                {{ tag }}
              </span>
            </div>
            
            <div class="job-footer">
              <div class="company-info">
                <img :src="job.companyLogo || defaultLogo" class="company-logo" alt="" />
                <div>
                  <div class="company-name">{{ job.companyName }}</div>
                  <div class="company-desc">{{ job.industry }} · {{ job.companyScale }}</div>
                </div>
              </div>
              <div class="publisher-info">
                <span>{{ job.publisherName }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="view-more">
          <el-button @click="$router.push('/jobs')">查看更多职位</el-button>
        </div>
      </div>
    </section>
    
    <!-- 特色功能 -->
    <section class="section features">
      <div class="container">
        <h2 class="section-title">
          <el-icon><Star /></el-icon>
          平台优势
        </h2>
        
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <h3>直接沟通</h3>
            <p>与HR在线直聊，了解职位详情</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Timer /></el-icon>
            </div>
            <h3>高效匹配</h3>
            <p>智能推荐，精准匹配心仪职位</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Checked /></el-icon>
            </div>
            <h3>真实可靠</h3>
            <p>企业资质认证，信息真实有效</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Coin /></el-icon>
            </div>
            <h3>薪资透明</h3>
            <p>薪资范围公开，待遇一目了然</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import { formatSalary, cityList } from '@/utils'
import { Search, Location, Promotion, Star, ChatDotRound, Timer, Checked, Coin } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const hotJobs = ref([])

const defaultLogo = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><rect fill="%2300bebd" width="40" height="40" rx="4"/><text x="20" y="26" text-anchor="middle" fill="white" font-size="16">Co</text></svg>'

const searchForm = ref({
  city: '北京',
  keyword: ''
})

const hotKeywords = ['Java', 'Python', '前端', '产品经理', 'UI设计', '运营', '销售']

async function loadHotJobs() {
  loading.value = true
  try {
    const res = await api.get('/jobs/hot', { params: { page: 1, size: 6 } })
    if (res.code === 200) {
      hotJobs.value = res.data.records || []
    }
  } catch (error) {
    console.error('Failed to load hot jobs:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  router.push({
    path: '/jobs',
    query: {
      keyword: searchForm.value.keyword,
      city: searchForm.value.city
    }
  })
}

function searchByKeyword(keyword) {
  searchForm.value.keyword = keyword
  handleSearch()
}

function goToJob(id) {
  router.push(`/jobs/${id}`)
}

onMounted(() => {
  loadHotJobs()
})
</script>

<style lang="scss" scoped>
.home-page {
  background: #fff;
}

.hero {
  background: linear-gradient(135deg, #00bebd 0%, #00a5a4 100%);
  padding: 80px 0;
  text-align: center;
  color: #fff;
  
  h1 {
    font-size: 42px;
    font-weight: 700;
    margin-bottom: 16px;
  }
  
  .subtitle {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 40px;
  }
  
  .hero-search {
    max-width: 700px;
    margin: 0 auto 24px;
    background: #fff;
    border-radius: 8px;
    padding: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    
    :deep(.el-input__wrapper) {
      box-shadow: none;
    }
    
    :deep(.el-button) {
      padding: 0 32px;
    }
  }
  
  .hot-keywords {
    .label {
      opacity: 0.8;
    }
    
    .keyword {
      margin: 0 8px;
      padding: 4px 12px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20px;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

.section {
  padding: 60px 0;
  
  .section-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 32px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      color: #00bebd;
    }
  }
}

.hot-jobs {
  background: #f5f6f7;
  
  .job-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .view-more {
    text-align: center;
    margin-top: 32px;
  }
}

.features {
  .feature-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
  }
  
  .feature-item {
    text-align: center;
    padding: 32px 20px;
    background: #f9fafb;
    border-radius: 12px;
    transition: transform 0.2s, box-shadow 0.2s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    .feature-icon {
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, #00bebd, #00a5a4);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;
      
      .el-icon {
        font-size: 28px;
        color: #fff;
      }
    }
    
    h3 {
      font-size: 18px;
      margin-bottom: 8px;
      color: #333;
    }
    
    p {
      color: #666;
      font-size: 14px;
    }
  }
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 28px;
  }
  
  .hot-jobs .job-list {
    grid-template-columns: 1fr;
  }
  
  .features .feature-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
