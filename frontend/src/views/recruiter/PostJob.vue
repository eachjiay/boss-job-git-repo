<template>
  <div class="post-job-page form-page">
    <div class="card">
      <div class="page-header">
        <h1>发布职位</h1>
        <p>填写职位信息，吸引优秀人才</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="job-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">1</span>
            职位基本信息
          </div>
          
          <el-form-item label="职位名称" prop="title">
            <el-input v-model="form.title" placeholder="请输入职位名称，如：Java开发工程师" />
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="招聘类型" prop="jobType">
                <el-radio-group v-model="form.jobType">
                  <el-radio-button value="社招全职">社招全职</el-radio-button>
                  <el-radio-button value="应届校园招聘">应届校园招聘</el-radio-button>
                  <el-radio-button value="实习生招聘">实习生招聘</el-radio-button>
                  <el-radio-button value="兼职招聘">兼职招聘</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工作城市" prop="city">
                <el-select v-model="form.city" placeholder="选择城市" style="width: 100%;">
                  <el-option v-for="city in cityList" :key="city" :label="city" :value="city" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="工作地点" prop="address">
            <el-input v-model="form.address" placeholder="详细工作地址" />
          </el-form-item>
          
          <el-form-item label="职位描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="6"
              placeholder="请输入职位描述，包括岗位职责、任职要求等"
            />
          </el-form-item>
        </div>
        
        <!-- 职位要求 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">2</span>
            职位要求
          </div>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="经验要求" prop="experience">
                <el-select v-model="form.experience" placeholder="选择经验要求" style="width: 100%;">
                  <el-option v-for="exp in experienceList" :key="exp" :label="exp" :value="exp" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学历要求" prop="education">
                <el-select v-model="form.education" placeholder="选择学历要求" style="width: 100%;">
                  <el-option v-for="edu in educationList" :key="edu" :label="edu" :value="edu" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="薪资范围 (K)" prop="salaryMin">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-input-number v-model="form.salaryMin" :min="1" :max="200" placeholder="最低" style="width: 100%;" />
              </el-col>
              <el-col :span="4" style="text-align: center; line-height: 32px;">
                至
              </el-col>
              <el-col :span="10">
                <el-input-number v-model="form.salaryMax" :min="1" :max="200" placeholder="最高" style="width: 100%;" />
              </el-col>
            </el-row>
          </el-form-item>
          
          <el-form-item label="职位关键词">
            <el-select 
              v-model="form.keywordList" 
              multiple 
              filterable 
              allow-create
              placeholder="输入并回车添加关键词"
              style="width: 100%;"
            >
              <el-option v-for="kw in suggestedKeywords" :key="kw" :label="kw" :value="kw" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="职位福利">
            <el-checkbox-group v-model="form.benefitList">
              <el-checkbox v-for="b in benefitOptions" :key="b" :label="b" :value="b" />
            </el-checkbox-group>
          </el-form-item>
        </div>
        
        <!-- 提交 -->
        <div class="form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布职位
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { cityList, educationList, experienceList } from '@/utils'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  title: '',
  jobType: '社招全职',
  city: '',
  address: '',
  description: '',
  experience: '',
  education: '',
  salaryMin: null,
  salaryMax: null,
  keywordList: [],
  benefitList: []
})

const rules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  jobType: [{ required: true, message: '请选择招聘类型', trigger: 'change' }],
  city: [{ required: true, message: '请选择工作城市', trigger: 'change' }],
  experience: [{ required: true, message: '请选择经验要求', trigger: 'change' }],
  education: [{ required: true, message: '请选择学历要求', trigger: 'change' }],
  salaryMin: [{ required: true, message: '请输入薪资范围', trigger: 'blur' }]
}

const suggestedKeywords = [
  'Java', 'Python', 'JavaScript', 'Vue', 'React', 'Node.js',
  'Spring Boot', 'MySQL', 'Redis', 'Docker', 'Kubernetes',
  '产品经理', 'UI设计', '数据分析', '运营', '销售'
]

const benefitOptions = [
  '五险一金', '带薪年假', '弹性工作', '免费三餐', '定期体检',
  '股票期权', '年终奖金', '节日福利', '交通补贴', '住房补贴'
]

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (form.salaryMin && form.salaryMax && form.salaryMin > form.salaryMax) {
    ElMessage.error('最低薪资不能高于最高薪资')
    return
  }
  
  submitting.value = true
  try {
    const data = {
      ...form,
      keywords: form.keywordList,  // 直接发送数组，后端DTO会处理
      benefits: form.benefitList   // 直接发送数组，后端DTO会处理
    }
    delete data.keywordList
    delete data.benefitList
    
    const res = await api.post('/jobs', data)
    if (res.code === 200) {
      ElMessage.success('职位发布成功')
      router.push('/recruiter/jobs')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('Failed to post job:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.post-job-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 32px;
  
  h1 {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
  }
}

.job-form {
  max-width: 800px;
}

.form-actions {
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
