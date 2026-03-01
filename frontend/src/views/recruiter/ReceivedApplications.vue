<template>
  <div class="received-applications-page">
    <div class="page-header">
      <h1>牛人管理</h1>
      <p>管理收到的简历投递</p>
    </div>
    
    <!-- 状态标签 -->
    <div class="status-tabs">
      <el-radio-group v-model="currentStatus" @change="loadApplications">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待处理</el-radio-button>
        <el-radio-button value="CHATTING">沟通中</el-radio-button>
        <el-radio-button value="INTERVIEW">已约面</el-radio-button>
        <el-radio-button value="OFFER">已发offer</el-radio-button>
        <el-radio-button value="HIRED">已入职</el-radio-button>
        <el-radio-button value="REJECTED">不合适</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 申请列表 -->
    <div class="application-list" v-loading="loading">
      <el-table :data="applications" style="width: 100%" stripe>
        <el-table-column label="候选人" width="180">
          <template #default="{ row }">
            <div class="candidate-cell">
              <el-avatar :size="40">{{ row.candidateName?.charAt(0) }}</el-avatar>
              <div class="candidate-info">
                <div class="name">{{ row.candidateName }}</div>
                <div class="meta">{{ row.candidateEducation }} · {{ row.candidateWorkYears || '应届' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="基本信息" width="200">
          <template #default="{ row }">
            <div>{{ row.candidateAge }}岁 · {{ row.candidateSchool }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="应聘职位">
          <template #default="{ row }">
            <div class="job-cell">
              <div class="job-title">{{ row.jobTitle }}</div>
              <div class="job-salary salary">{{ formatSalary(row.jobSalaryMin, row.jobSalaryMax) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="AI 匹配度" width="120">
          <template #default="{ row }">
            <div v-if="row.aiResult" class="ai-score-cell">
              <el-progress 
                type="circle" 
                :percentage="row.aiResult.score" 
                :width="40" 
                :color="getScoreColor(row.aiResult.score)"
              />
              <el-tooltip :content="row.aiResult.reason.join(', ')" placement="top">
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <el-button 
              v-else 
              type="primary" 
              link 
              size="small" 
              :loading="row.analyzing"
              @click="handleAnalyze(row)"
            >
              AI 诊断
            </el-button>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="投递时间" width="120">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button type="info" plain @click="viewResume(row.candidateId)">
                查看简历
              </el-button>
              <el-button type="primary" @click="updateStatus(row, 'CHATTING')">
                沟通
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => updateStatus(row, cmd)">
                <el-button>
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="INTERVIEW">标记为已约面</el-dropdown-item>
                    <el-dropdown-item command="OFFER">标记为已发offer</el-dropdown-item>
                    <el-dropdown-item command="HIRED">标记为已入职</el-dropdown-item>
                    <el-dropdown-item command="REJECTED" divided>标记为不合适</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="applications.length === 0 && !loading" class="empty-state">
        <el-icon><Document /></el-icon>
        <p>暂无投递记录</p>
      </div>
    </div>
    
    <!-- 分页 -->
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
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { formatSalary, formatTime, applicationStatusMap } from '@/utils'
import { ArrowDown, Document, InfoFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const applications = ref([])
const total = ref(0)
const currentStatus = ref('')
const page = ref(1)

const router = useRouter()

function viewResume(userId) {
  router.push(`/resumes/${userId}`)
}

async function loadApplications() {
  loading.value = true
  try {
    const res = await api.get('/applications/received', {
      params: {
        status: currentStatus.value || undefined,
        page: page.value,
        size: 10
      }
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

async function updateStatus(row, status) {
  try {
    const res = await api.put(`/applications/${row.id}/status`, null, {
      params: { status }
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      row.status = status
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('Failed to update status:', error)
  }
}

async function handleAnalyze(row) {
  row.analyzing = true
  try {
    const res = await api.get(`/applications/${row.id}/ai-match`)
    if (res.code === 200) {
      if (typeof res.data === 'string') {
        row.aiResult = JSON.parse(res.data)
      } else {
        row.aiResult = res.data
      }
    }
  } catch (error) {
    console.error('AI Analysis failed:', error)
  } finally {
    row.analyzing = false
  }
}

function getScoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

function getStatusType(status) {
  return applicationStatusMap[status]?.type || 'info'
}

function getStatusText(status) {
  return applicationStatusMap[status]?.text || status
}

onMounted(() => {
  loadApplications()
})
</script>

<style lang="scss" scoped>
.received-applications-page {
  max-width: 1200px;
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

.status-tabs {
  margin-bottom: 20px;
}

.candidate-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .name {
    font-weight: 500;
  }
  
  .meta {
    font-size: 12px;
    color: #999;
  }
}

.job-cell {
  .job-title {
    margin-bottom: 4px;
  }
  
  .job-salary {
    font-size: 13px;
  }
}

.ai-score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .info-icon {
    font-size: 16px;
    color: #909399;
    cursor: pointer;
  }
}
</style>
