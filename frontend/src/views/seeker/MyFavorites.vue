<template>
  <div class="my-favorites">
    <div class="page-header">
      <h2>我的收藏</h2>
      <span class="count">共 {{ total }} 个职位</span>
    </div>

    <div v-loading="loading" class="job-list">
      <template v-if="jobs.length">
        <div v-for="job in jobs" :key="job.id" class="job-card" @click="goToJob(job.id)">
          <div class="job-main">
            <h3 class="job-title">{{ job.title }}</h3>
            <div class="job-salary">{{ formatSalary(job.salaryMin, job.salaryMax) }}</div>
          </div>
          <div class="job-meta">
            <span class="company">{{ job.companyName }}</span>
            <span>{{ job.city }}</span>
            <span>{{ job.experience }}</span>
            <span>{{ job.education }}</span>
          </div>
          <div class="job-tags">
            <span v-for="tag in (job.benefits || []).slice(0, 4)" :key="tag" class="tag">{{ tag }}</span>
          </div>
          <div class="job-actions">
            <el-button type="danger" size="small" @click.stop="removeFavorite(job.id)">
              取消收藏
            </el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-else-if="!loading" description="暂无收藏的职位" />
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { formatSalary } from '@/utils'

const router = useRouter()

const loading = ref(false)
const jobs = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadFavorites() {
  loading.value = true
  try {
    const res = await api.get('/favorites/my', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      jobs.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    loading.value = false
  }
}

async function removeFavorite(jobId) {
  try {
    await api.delete(`/favorites/${jobId}`)
    ElMessage.success('已取消收藏')
    // 从列表中移除
    jobs.value = jobs.value.filter(j => j.id !== jobId)
    total.value--
  } catch (error) {
    console.error('Failed to remove favorite:', error)
    ElMessage.error('操作失败')
  }
}

function goToJob(jobId) {
  router.push(`/jobs/${jobId}`)
}

function handlePageChange(page) {
  currentPage.value = page
  loadFavorites()
}

onMounted(() => {
  loadFavorites()
})
</script>

<style lang="scss" scoped>
.my-favorites {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  h2 {
    margin: 0;
    font-size: 20px;
  }

  .count {
    color: #999;
    font-size: 14px;
  }
}

.job-list {
  min-height: 300px;
}

.job-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .job-main {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .job-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      color: #333;
    }

    .job-salary {
      font-size: 18px;
      font-weight: 600;
      color: #00bebd;
    }
  }

  .job-meta {
    display: flex;
    gap: 16px;
    font-size: 14px;
    color: #666;
    margin-bottom: 12px;

    .company {
      font-weight: 500;
      color: #333;
    }
  }

  .job-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 12px;

    .tag {
      padding: 4px 8px;
      background: #f5f5f5;
      border-radius: 4px;
      font-size: 12px;
      color: #666;
    }
  }

  .job-actions {
    display: flex;
    justify-content: flex-end;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
