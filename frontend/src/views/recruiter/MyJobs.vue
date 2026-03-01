<template>
  <div class="my-jobs-page">
    <div class="page-header">
      <h1>职位管理</h1>
      <el-button type="primary" @click="$router.push('/recruiter/post-job')">
        <el-icon><Plus /></el-icon>
        发布新职位
      </el-button>
    </div>
    
    <div class="job-list" v-loading="loading">
      <el-table :data="jobs" style="width: 100%">
        <el-table-column label="职位名称" min-width="200">
          <template #default="{ row }">
            <div class="job-title" @click="viewJob(row)">{{ row.title }}</div>
            <div class="job-meta">{{ row.city }} · {{ row.experience }} · {{ row.education }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="薪资" width="120">
          <template #default="{ row }">
            <span class="salary">{{ formatSalary(row.salaryMin, row.salaryMax) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="浏览量" width="100" prop="viewCount" />
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '招聘中' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="发布时间" width="120">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="editJob(row)">编辑</el-button>
            <el-button 
              size="small" 
              text 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下线' : '上线' }}
            </el-button>
            <el-button size="small" text type="danger" @click="deleteJob(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="jobs.length === 0 && !loading" class="empty-state">
        <el-icon><Document /></el-icon>
        <p>暂无发布的职位</p>
        <el-button type="primary" @click="$router.push('/recruiter/post-job')">发布第一个职位</el-button>
      </div>
    </div>
    
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="10"
        layout="total, prev, pager, next"
        @current-change="loadJobs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import { formatSalary, formatTime } from '@/utils'
import { Plus, Document } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const page = ref(1)

async function loadJobs() {
  loading.value = true
  try {
    // 获取当前用户发布的职位
    const res = await api.get('/jobs/search', { 
      params: { page: page.value, size: 10 } 
    })
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

function viewJob(job) {
  router.push(`/jobs/${job.id}`)
}

function editJob(job) {
  ElMessage.info('编辑功能开发中')
}

async function toggleStatus(job) {
  try {
    if (job.status === 1) {
      await api.delete(`/jobs/${job.id}`)
      job.status = 0
      ElMessage.success('职位已下线')
    } else {
      // 重新上线需要更新状态
      await api.put(`/jobs/${job.id}`, { ...job, status: 1 })
      job.status = 1
      ElMessage.success('职位已上线')
    }
  } catch (error) {
    console.error('Failed to toggle status:', error)
  }
}

async function deleteJob(job) {
  try {
    await ElMessageBox.confirm('确定要删除这个职位吗？', '确认删除', {
      type: 'warning'
    })
    await api.delete(`/jobs/${job.id}`)
    ElMessage.success('删除成功')
    loadJobs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete job:', error)
    }
  }
}

onMounted(() => {
  loadJobs()
})
</script>

<style lang="scss" scoped>
.my-jobs-page {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h1 {
    font-size: 24px;
  }
}

.job-title {
  font-weight: 500;
  cursor: pointer;
  
  &:hover {
    color: #00bebd;
  }
}

.job-meta {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
