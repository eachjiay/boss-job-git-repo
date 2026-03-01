<template>
  <div class="candidate-search-page">
    <div class="page-header">
      <h1>人才库</h1>
      <p>搜索合适的候选人</p>
    </div>
    
    <!-- 搜索框 -->
    <div class="search-box">
      <el-select v-model="searchForm.city" placeholder="城市" clearable style="width: 120px;">
        <el-option v-for="city in cityList" :key="city" :label="city" :value="city" />
      </el-select>
      <el-select v-model="searchForm.education" placeholder="学历" clearable style="width: 100px;">
        <el-option v-for="edu in educationList" :key="edu" :label="edu" :value="edu" />
      </el-select>
      <el-select v-model="searchForm.workYears" placeholder="经验" clearable style="width: 120px;">
        <el-option v-for="exp in experienceList" :key="exp" :label="exp" :value="exp" />
      </el-select>
      <el-input 
        v-model="searchForm.keyword" 
        placeholder="搜索技能、职位、姓名"
        clearable
        style="flex: 1;"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>
    
    <!-- 候选人列表 -->
    <div class="candidate-list" v-loading="loading">
      <template v-if="candidates.length > 0">
        <div v-for="candidate in candidates" :key="candidate.id" class="candidate-card">
          <el-avatar :size="56" :src="candidate.avatar">
            {{ candidate.realName?.charAt(0) }}
          </el-avatar>
          
          <div class="candidate-info">
            <div class="candidate-name">
              {{ candidate.realName }}
              <el-tag size="small" :type="getJobStatusType(candidate.jobStatus)">
                {{ candidate.jobStatus }}
              </el-tag>
            </div>
            <div class="candidate-meta">
              {{ candidate.age }}岁 · {{ candidate.workYears || '应届' }} · {{ candidate.education }} · {{ candidate.school }}
            </div>
            <div class="candidate-tags">
              <span v-for="skill in (candidate.skills || []).slice(0, 5)" :key="skill" class="tag">
                {{ skill }}
              </span>
            </div>
          </div>
          
          <div class="candidate-expect">
            <div class="expect-city">期望: {{ candidate.expectCity }}</div>
            <div class="expect-position">{{ candidate.expectPosition }}</div>
            <div class="expect-salary salary">
              {{ formatSalary(candidate.expectSalaryMin, candidate.expectSalaryMax) }}
            </div>
          </div>
          
          <div class="candidate-actions">
            <el-button type="primary" size="small" @click="startChat(candidate)">
              <el-icon><ChatDotRound /></el-icon>
              沟通
            </el-button>
            <el-button size="small" @click="viewResume(candidate)">
              查看简历
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-else-if="!loading" class="empty-state">
        <el-icon><User /></el-icon>
        <p>暂无符合条件的候选人</p>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadCandidates"
      />
    </div>
    
    <!-- 简历弹窗 -->
    <el-dialog v-model="resumeDialogVisible" title="候选人简历" width="700px">
      <div v-if="selectedCandidate" class="resume-preview">
        <div class="resume-header">
          <el-avatar :size="64">{{ selectedCandidate.realName?.charAt(0) }}</el-avatar>
          <div class="resume-info">
            <h2>{{ selectedCandidate.realName }}</h2>
            <p>{{ selectedCandidate.education }} · {{ selectedCandidate.school }} · {{ selectedCandidate.major }}</p>
          </div>
        </div>
        
        <div class="resume-section">
          <h3>个人优势</h3>
          <p>{{ selectedCandidate.introduction || '暂无介绍' }}</p>
        </div>
        
        <div class="resume-section">
          <h3>专业技能</h3>
          <div class="skills">
            <span v-for="skill in selectedCandidate.skills" :key="skill" class="tag">{{ skill }}</span>
          </div>
        </div>
        
        <div class="resume-section">
          <h3>求职意向</h3>
          <p>期望城市: {{ selectedCandidate.expectCity }}</p>
          <p>期望职位: {{ selectedCandidate.expectPosition }}</p>
          <p>期望薪资: {{ formatSalary(selectedCandidate.expectSalaryMin, selectedCandidate.expectSalaryMax) }}</p>
        </div>
        
        <!-- 附件简历 -->
        <div class="resume-section attachment-section">
          <h3>附件简历</h3>
          <div v-if="selectedCandidate.attachmentUrl" class="attachment-download">
            <el-icon><Document /></el-icon>
            <a :href="selectedCandidate.attachmentUrl" target="_blank" class="download-link">
              点击查看/下载附件简历
            </a>
          </div>
          <p v-else class="no-attachment">该求职者暂未上传附件简历</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { formatSalary, cityList, educationList, experienceList } from '@/utils'
import { Search, ChatDotRound, User, Document } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const candidates = ref([])
const total = ref(0)
const resumeDialogVisible = ref(false)
const selectedCandidate = ref(null)

const searchForm = reactive({
  keyword: '',
  city: '',
  education: '',
  workYears: '',
  page: 1,
  size: 10
})

async function loadCandidates() {
  loading.value = true
  try {
    const res = await api.get('/resumes/search', { params: searchForm })
    if (res.code === 200) {
      candidates.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load candidates:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchForm.page = 1
  loadCandidates()
}

function getJobStatusType(status) {
  if (status?.includes('离职')) return 'success'
  if (status?.includes('在职')) return 'warning'
  return 'info'
}

async function startChat(candidate) {
  try {
    // 发起沟通
    const res = await api.post('/messages/start-conversation', {
      receiverId: candidate.userId,
      content: `您好，看到您的简历很不错，对「${candidate.expectPosition || '相关职位'}」感兴趣吗？希望能和您进一步沟通！`
    })
    
    if (res.code === 200) {
      ElMessage.success('已发起沟通')
      router.push(`/messages/${candidate.userId}`)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('Failed to start chat:', error)
    ElMessage.error('发起沟通失败')
  }
}

function viewResume(candidate) {
  selectedCandidate.value = candidate
  resumeDialogVisible.value = true
}

onMounted(() => {
  loadCandidates()
})
</script>

<style lang="scss" scoped>
.candidate-search-page {
  max-width: 1000px;
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

.resume-preview {
  .resume-header {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e8e8e8;
    
    h2 {
      font-size: 20px;
      margin-bottom: 8px;
    }
    
    p {
      color: #666;
    }
  }
  
  .resume-section {
    margin-bottom: 20px;
    
    h3 {
      font-size: 15px;
      color: #00bebd;
      margin-bottom: 12px;
    }
    
    p {
      color: #666;
      line-height: 1.6;
    }
    
    .skills {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
  
  .attachment-section {
    .attachment-download {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: #f5f7fa;
      border-radius: 6px;
      
      .el-icon {
        font-size: 20px;
        color: #00bebd;
      }
      
      .download-link {
        color: #00bebd;
        text-decoration: none;
        font-weight: 500;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
    
    .no-attachment {
      color: #999;
      font-style: italic;
    }
  }
}
</style>
