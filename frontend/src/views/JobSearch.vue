<template>
  <div class="job-search-page">
    <div class="container">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-select v-model="searchForm.city" placeholder="城市" clearable>
          <el-option v-for="city in cityList" :key="city" :label="city" :value="city" />
        </el-select>
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索职位、公司、关键词"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
      
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">学历要求</span>
          <div class="filter-options">
            <span 
              v-for="edu in educationList" 
              :key="edu"
              class="filter-option"
              :class="{ active: searchForm.education === edu }"
              @click="setFilter('education', edu)"
            >
              {{ edu }}
            </span>
          </div>
        </div>
        
        <div class="filter-row">
          <span class="filter-label">经验要求</span>
          <div class="filter-options">
            <span 
              v-for="exp in experienceList" 
              :key="exp"
              class="filter-option"
              :class="{ active: searchForm.experience === exp }"
              @click="setFilter('experience', exp)"
            >
              {{ exp }}
            </span>
          </div>
        </div>
        
        <div class="filter-row">
          <span class="filter-label">薪资范围</span>
          <div class="filter-options">
            <span 
              v-for="range in salaryRanges" 
              :key="range.label"
              class="filter-option"
              :class="{ active: currentSalaryLabel === range.label }"
              @click="setSalaryFilter(range)"
            >
              {{ range.label }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 排序和结果统计 -->
      <div class="result-header">
        <div class="result-count">
          共找到 <strong>{{ total }}</strong> 个职位
        </div>
        <div class="sort-options">
          <span 
            class="sort-option"
            :class="{ active: searchForm.sort === 'default' }"
            @click="searchForm.sort = 'default'; loadJobs()"
          >
            综合排序
          </span>
          <span 
            class="sort-option"
            :class="{ active: searchForm.sort === 'latest' }"
            @click="searchForm.sort = 'latest'; loadJobs()"
          >
            最新优先
          </span>
          <span 
            class="sort-option"
            :class="{ active: searchForm.sort === 'salary' }"
            @click="searchForm.sort = 'salary'; loadJobs()"
          >
            薪资优先
          </span>
        </div>
      </div>
      
      <!-- 职位列表 -->
      <div class="job-list" v-loading="loading">
        <template v-if="jobs.length > 0">
          <div 
            v-for="job in jobs" 
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
              <span v-for="tag in (job.keywords || []).slice(0, 5)" :key="tag" class="tag">
                {{ tag }}
              </span>
            </div>
            
            <div class="job-footer">
              <div class="company-info" @click.stop="goToCompany(job.companyId)">
                <img :src="job.companyLogo || defaultLogo" class="company-logo" alt="" />
                <div>
                  <div class="company-name">{{ job.companyName }}</div>
                  <div class="company-desc">{{ job.industry }} · {{ job.companyScale }} · {{ job.financing }}</div>
                </div>
              </div>
              <div class="publisher-info">
                <el-avatar :size="28">{{ job.publisherName?.charAt(0) }}</el-avatar>
                <span>{{ job.publisherName }}</span>
              </div>
            </div>
          </div>
        </template>
        
        <div v-else-if="!loading" class="empty-state">
          <el-icon><Document /></el-icon>
          <p>暂无符合条件的职位</p>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadJobs"
          @current-change="loadJobs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/utils/api'
import { formatSalary, cityList, educationList, experienceList, salaryRanges } from '@/utils'
import { Search, Location, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const jobs = ref([])
const total = ref(0)

const defaultLogo = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><rect fill="%2300bebd" width="40" height="40" rx="4"/><text x="20" y="26" text-anchor="middle" fill="white" font-size="16">Co</text></svg>'

const searchForm = ref({
  keyword: '',
  city: '',
  education: '不限',
  experience: '不限',
  salaryMin: null,
  salaryMax: null,
  sort: 'default',
  page: 1,
  size: 10
})

const currentSalaryLabel = computed(() => {
  const range = salaryRanges.find(
    r => r.min === searchForm.value.salaryMin && r.max === searchForm.value.salaryMax
  )
  return range?.label || '不限'
})

async function loadJobs() {
  loading.value = true
  try {
    const params = { ...searchForm.value }
    if (params.education === '不限') delete params.education
    if (params.experience === '不限') delete params.experience
    
    const res = await api.get('/jobs/search', { params })
    if (res.code === 200) {
      jobs.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load jobs:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchForm.value.page = 1
  loadJobs()
}

function setFilter(key, value) {
  searchForm.value[key] = value
  searchForm.value.page = 1
  loadJobs()
}

function setSalaryFilter(range) {
  searchForm.value.salaryMin = range.min
  searchForm.value.salaryMax = range.max
  searchForm.value.page = 1
  loadJobs()
}

function goToJob(id) {
  router.push(`/jobs/${id}`)
}

function goToCompany(id) {
  router.push(`/companies/${id}`)
}

// 从URL参数初始化
onMounted(() => {
  if (route.query.keyword) {
    searchForm.value.keyword = route.query.keyword
  }
  if (route.query.city) {
    searchForm.value.city = route.query.city
  }
  loadJobs()
})

// 监听路由变化
watch(() => route.query, (query) => {
  if (query.keyword !== undefined) {
    searchForm.value.keyword = query.keyword
  }
  if (query.city !== undefined) {
    searchForm.value.city = query.city
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
.job-search-page {
  padding: 20px 0;
  min-height: calc(100vh - 60px);
  background: #f5f6f7;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .result-count {
    color: #666;
    
    strong {
      color: #00bebd;
    }
  }
  
  .sort-options {
    display: flex;
    gap: 20px;
    
    .sort-option {
      color: #666;
      cursor: pointer;
      padding: 4px 0;
      border-bottom: 2px solid transparent;
      
      &:hover {
        color: #00bebd;
      }
      
      &.active {
        color: #00bebd;
        border-bottom-color: #00bebd;
      }
    }
  }
}
</style>
