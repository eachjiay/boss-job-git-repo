<template>
  <div class="my-applications-page">
    <div class="page-header">
      <h1>投递记录</h1>
      <p>查看你投递的所有职位</p>
    </div>
    
    <div class="applications-list" v-loading="loading">
      <template v-if="applications.length > 0">
        <div 
          v-for="app in applications" 
          :key="app.id" 
          class="application-card card"
        >
          <div class="app-main" @click="goToJob(app.jobId)">
            <div class="job-info">
              <div class="job-title">{{ app.jobTitle }}</div>
              <div class="job-salary salary">{{ formatSalary(app.jobSalaryMin, app.jobSalaryMax) }}</div>
            </div>
            <div class="company-info">
              <img :src="app.companyLogo || defaultLogo" class="company-logo" alt="" />
              <span class="company-name">{{ app.companyName }}</span>
              <span class="job-city">{{ app.jobCity }}</span>
            </div>
          </div>
          
          <div class="app-status">
            <el-tag :type="getStatusType(app.status)" size="large">
              {{ getStatusText(app.status) }}
            </el-tag>
            <div class="app-time">{{ formatTime(app.createTime) }} 投递</div>
          </div>
        </div>
      </template>
      
      <div v-else-if="!loading" class="empty-state">
        <el-icon><Document /></el-icon>
        <p>暂无投递记录</p>
        <el-button type="primary" @click="$router.push('/jobs')">去看看职位</el-button>
      </div>
    </div>
    
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="10"
        layout="total, prev, pager, next"
        @current-change="loadApplications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import { formatSalary, formatTime, applicationStatusMap } from '@/utils'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const applications = ref([])
const total = ref(0)
const page = ref(1)

const defaultLogo = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><rect fill="%2300bebd" width="40" height="40" rx="4"/><text x="20" y="26" text-anchor="middle" fill="white" font-size="16">Co</text></svg>'

async function loadApplications() {
  loading.value = true
  try {
    const res = await api.get('/applications/my', {
      params: { page: page.value, size: 10 }
    })
    if (res.code === 200) {
      applications.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load applications:', error)
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  return applicationStatusMap[status]?.type || 'info'
}

function getStatusText(status) {
  return applicationStatusMap[status]?.text || status
}

function goToJob(jobId) {
  router.push(`/jobs/${jobId}`)
}

onMounted(() => {
  loadApplications()
})
</script>

<style lang="scss" scoped>
.my-applications-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
  }
}

.application-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: box-shadow 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .app-main {
    flex: 1;
  }
  
  .job-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  
  .job-title {
    font-size: 16px;
    font-weight: 600;
    
    &:hover {
      color: #00bebd;
    }
  }
  
  .job-salary {
    font-size: 16px;
  }
  
  .company-info {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #666;
    
    .company-logo {
      width: 24px;
      height: 24px;
      border-radius: 4px;
    }
  }
  
  .app-status {
    text-align: right;
    padding-left: 24px;
    
    .app-time {
      font-size: 12px;
      color: #999;
      margin-top: 8px;
    }
  }
}
</style>
