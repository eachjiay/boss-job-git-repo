<template>
  <div class="my-resume-page">
    <div class="page-header">
      <h1>我的简历</h1>
      <p>完善简历信息，让HR更快找到你</p>
    </div>
    
    <div class="resume-form card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">1</span>
            基本信息
          </div>
          
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio value="男">男</el-radio>
                  <el-radio value="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="年龄" prop="age">
                <el-input-number 
                  v-model="form.age" 
                  :min="18" 
                  :max="60" 
                  :controls="false"
                  placeholder="请输入"
                  style="width: 100%;" 
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 教育经历 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">2</span>
            教育背景
          </div>
          
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="最高学历" prop="education">
                <el-select v-model="form.education" placeholder="选择学历" style="width: 100%;">
                  <el-option v-for="edu in educationList.slice(1)" :key="edu" :label="edu" :value="edu" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="毕业院校" prop="school">
                <el-input v-model="form.school" placeholder="请输入学校名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="专业" prop="major">
                <el-input v-model="form.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="毕业年份">
                <el-date-picker
                  v-model="form.graduationYear"
                  type="year"
                  placeholder="选择年份"
                  style="width: 100%;"
                  value-format="YYYY"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="工作年限">
                <el-select v-model="form.workYears" placeholder="选择工作年限" style="width: 100%;">
                  <el-option v-for="exp in experienceList" :key="exp" :label="exp" :value="exp" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="求职状态">
                <el-select v-model="form.jobStatus" placeholder="选择求职状态" style="width: 100%;">
                  <el-option v-for="s in jobStatusList" :key="s" :label="s" :value="s" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 求职意向 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">3</span>
            求职意向
          </div>
          
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="期望城市" prop="expectCity">
                <el-select v-model="form.expectCity" placeholder="选择城市" style="width: 100%;">
                  <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="期望职位" prop="expectPosition">
                <el-input v-model="form.expectPosition" placeholder="如：Java开发工程师" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="期望薪资 (K)">
                <el-row :gutter="8">
                  <el-col :span="11">
                    <el-input-number 
                      v-model="form.expectSalaryMin" 
                      :min="1" 
                      :controls="false"
                      placeholder="最低" 
                      style="width: 100%;" 
                    />
                  </el-col>
                  <el-col :span="2" style="text-align: center;">-</el-col>
                  <el-col :span="11">
                    <el-input-number 
                      v-model="form.expectSalaryMax" 
                      :min="1" 
                      :controls="false"
                      placeholder="最高" 
                      style="width: 100%;" 
                    />
                  </el-col>
                </el-row>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 个人介绍 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">4</span>
            个人介绍
          </div>
          
          <el-form-item label="个人优势">
            <el-input 
              v-model="form.introduction" 
              type="textarea" 
              :rows="4"
              placeholder="介绍你的核心竞争力、职业目标等"
            />
          </el-form-item>
          
          <el-form-item label="专业技能">
            <el-select 
              v-model="form.skillList" 
              multiple 
              filterable 
              allow-create
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="4"
              placeholder="输入技能并回车添加"
              style="width: 100%;"
            >
              <el-option v-for="s in suggestedSkills" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
        </div>
        
        <!-- 实习经历 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">5</span>
            实习经历
            <el-button type="primary" link size="small" @click="addInternship" style="float: right;">+ 添加</el-button>
          </div>
          <div v-for="(item, index) in form.internshipList" :key="index" class="experience-item">
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="公司名称" :prop="'internshipList.' + index + '.company'" :rules="{ required: true, message: '请输入公司名称', trigger: 'blur' }">
                      <el-input v-model="item.company" placeholder="请输入公司名称" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="职位" :prop="'internshipList.' + index + '.position'" :rules="{ required: true, message: '请输入职位', trigger: 'blur' }">
                      <el-input v-model="item.position" placeholder="请输入职位" />
                   </el-form-item>
                </el-col>
             </el-row>
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="开始时间">
                      <el-date-picker v-model="item.startDate" type="month" placeholder="开始时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="结束时间">
                      <el-date-picker v-model="item.endDate" type="month" placeholder="结束时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
             </el-row>
             <el-form-item label="工作内容">
                <el-input v-model="item.description" type="textarea" :rows="3" placeholder="描述你的实习内容和成果" />
             </el-form-item>
             <el-button type="danger" link size="small" @click="removeInternship(index)" v-if="form.internshipList.length > 0">删除</el-button>
             <el-divider v-if="index < form.internshipList.length - 1" />
          </div>
        </div>

        <!-- 工作经历 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">6</span>
            工作经历
             <el-button type="primary" link size="small" @click="addWork" style="float: right;">+ 添加</el-button>
          </div>
           <div v-for="(item, index) in form.workList" :key="index" class="experience-item">
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="公司名称" :prop="'workList.' + index + '.company'" :rules="{ required: true, message: '请输入公司名称', trigger: 'blur' }">
                      <el-input v-model="item.company" placeholder="请输入公司名称" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="职位" :prop="'workList.' + index + '.position'" :rules="{ required: true, message: '请输入职位', trigger: 'blur' }">
                      <el-input v-model="item.position" placeholder="请输入职位" />
                   </el-form-item>
                </el-col>
             </el-row>
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="开始时间">
                      <el-date-picker v-model="item.startDate" type="month" placeholder="开始时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="结束时间">
                      <el-date-picker v-model="item.endDate" type="month" placeholder="结束时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
             </el-row>
             <el-form-item label="工作内容">
                <el-input v-model="item.description" type="textarea" :rows="3" placeholder="描述你的工作内容和业绩" />
             </el-form-item>
              <el-button type="danger" link size="small" @click="removeWork(index)" v-if="form.workList.length > 0">删除</el-button>
             <el-divider v-if="index < form.workList.length - 1" />
          </div>
        </div>

        <!-- 项目经历 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">7</span>
            项目经历
             <el-button type="primary" link size="small" @click="addProject" style="float: right;">+ 添加</el-button>
          </div>
           <div v-for="(item, index) in form.projectList" :key="index" class="experience-item">
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="项目名称" :prop="'projectList.' + index + '.projectName'" :rules="{ required: true, message: '请输入项目名称', trigger: 'blur' }">
                      <el-input v-model="item.projectName" placeholder="请输入项目名称" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="负责角色" :prop="'projectList.' + index + '.role'">
                      <el-input v-model="item.role" placeholder="请输入你的角色" />
                   </el-form-item>
                </el-col>
             </el-row>
             <el-row :gutter="24">
                <el-col :span="12">
                   <el-form-item label="开始时间">
                      <el-date-picker v-model="item.startDate" type="month" placeholder="开始时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
                <el-col :span="12">
                   <el-form-item label="结束时间">
                      <el-date-picker v-model="item.endDate" type="month" placeholder="结束时间" value-format="YYYY-MM" style="width: 100%;" />
                   </el-form-item>
                </el-col>
             </el-row>
              <el-form-item label="项目链接">
                 <el-input v-model="item.link" placeholder="可选填项目链接" />
              </el-form-item>
             <el-form-item label="项目描述">
                <el-input v-model="item.description" type="textarea" :rows="3" placeholder="描述项目背景、技术栈和你的贡献" />
             </el-form-item>
              <el-button type="danger" link size="small" @click="removeProject(index)" v-if="form.projectList.length > 0">删除</el-button>
             <el-divider v-if="index < form.projectList.length - 1" />
          </div>
        </div>

        <!-- 附件简历 -->
        <div class="form-section">
          <div class="section-title">
            <span class="section-number">8</span>
            附件简历
          </div>
          
          <div class="upload-area">
            <el-upload
              class="resume-uploader"
              action="#"
              :http-request="handleUpload"
              :show-file-list="false"
              accept=".pdf,.doc,.docx"
            >
              <el-button type="primary" :loading="uploading">上传附件简历</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF, Word 格式，上传后可使用AI自动解析
                </div>
              </template>
            </el-upload>

            <div v-if="form.attachmentUrl" class="attachment-card-pro">
              <div class="file-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ getFileName(form.attachmentUrl) }}</div>
                <div class="file-meta">已上传，可用于 AI 智能填表</div>
              </div>
              <div class="file-actions">
                <el-button 
                  type="success" 
                  size="default" 
                  :loading="parsing"
                  @click="handleAiParse"
                  class="ai-primary-btn"
                >
                  <el-icon><MagicStick /></el-icon>
                  AI 智能解析
                </el-button>
                <el-button type="danger" link @click="removeAttachment">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-actions">
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            保存简历
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import api from '@/utils/api'
import { cityList, educationList, experienceList } from '@/utils'

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  realName: '',
  gender: '',
  age: null,
  phone: '',
  email: '',
  education: '',
  school: '',
  major: '',
  graduationYear: null,
  workYears: '',
  jobStatus: '',
  expectCity: '',
  expectPosition: '',
  expectSalaryMin: null,
  expectSalaryMax: null,
  introduction: '',
  skillList: [],
  internshipList: [],
  workList: [],
  projectList: [],
  attachmentUrl: ''
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  education: [{ required: true, message: '请选择学历', trigger: 'change' }],
  expectCity: [{ required: true, message: '请选择期望城市', trigger: 'change' }],
  expectPosition: [{ required: true, message: '请输入期望职位', trigger: 'blur' }]
}

const jobStatusList = [
  '离职-随时到岗',
  '在职-考虑机会',
  '在职-暂不考虑',
  '在校-月内到岗',
  '在校-考虑实习'
]

const suggestedSkills = [
  'Java', 'Python', 'JavaScript', 'TypeScript', 'Vue', 'React', 'Node.js',
  'Spring Boot', 'MySQL', 'Redis', 'Docker', 'Kubernetes', 'Linux',
  'Git', 'SQL', 'HTML/CSS', 'C++', 'Go', 'Rust'
]

// 添加条目函数
function addInternship() {
  form.internshipList.push({ company: '', position: '', startDate: '', endDate: '', description: '' })
}
function removeInternship(index) {
  form.internshipList.splice(index, 1)
}

function addWork() {
  form.workList.push({ company: '', position: '', startDate: '', endDate: '', description: '' })
}
function removeWork(index) {
  form.workList.splice(index, 1)
}

function addProject() {
  form.projectList.push({ projectName: '', role: '', startDate: '', endDate: '', description: '', link: '' })
}
function removeProject(index) {
  form.projectList.splice(index, 1)
}

async function loadResume() {
  loading.value = true
  try {
    const res = await api.get('/resumes/my')
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(form, {
        realName: data.realName || '',
        gender: data.gender || '',
        age: data.age,
        phone: data.phone || '',
        email: data.email || '',
        education: data.education || '',
        school: data.school || '',
        major: data.major || '',
        graduationYear: data.graduationYear?.toString(),
        workYears: data.workYears || '',
        jobStatus: data.jobStatus || '',
        expectCity: data.expectCity || '',
        expectPosition: data.expectPosition || '',
        expectSalaryMin: data.expectSalaryMin,
        expectSalaryMax: data.expectSalaryMax,
        introduction: data.introduction || '',
        skillList: parseJson(data.skills, []),
        internshipList: parseJson(data.internshipExperience, []),
        workList: parseJson(data.workExperience, []),
        projectList: parseJson(data.projectExperience, []),
        attachmentUrl: data.attachmentUrl || ''
      })
    }
  } catch (error) {
    console.log('No resume found')
  } finally {
    loading.value = false
  }
}

function parseJson(str, defaultValue) {
  if (!str) return defaultValue
  try {
     return typeof str === 'string' ? JSON.parse(str) : str
  } catch (e) {
    console.error('JSON parse error', e)
    return defaultValue
  }
}

const uploading = ref(false)

async function handleUpload(options) {
  const { file } = options
  
  // 验证文件大小 (例如 10MB)
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    // api.js 默认是 json，上传需要 multipart/form-data
    // axios 传递 FormData 会自动设置 Content-Type，但为了保险可以手动覆盖 headers
    const res = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (res.code === 200) {
      form.attachmentUrl = res.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('Upload failed:', error)
    ElMessage.error('上传失败，请检查网络或后端服务')
  } finally {
    uploading.value = false
  }
}

function getFileName(url) {
  if (!url) return ''
  const name = url.substring(url.lastIndexOf('/') + 1)
  // 如果是UUID风格的长名字，尝试缩短展示
  return name.length > 20 ? name.substring(0, 8) + '...' + name.substring(name.length - 12) : name
}

function removeAttachment() {
  ElMessageBox.confirm('确定删除已上传的附件简历吗？', '提示', {
    type: 'warning'
  }).then(() => {
    form.attachmentUrl = ''
  })
}

// AI 解析简历
const parsing = ref(false)

async function handleAiParse() {
  if (!form.attachmentUrl) {
    ElMessage.error('请先上传附件简历')
    return
  }

  try {
    await ElMessageBox.confirm(
      '将使用AI解析附件简历并自动填充表单，现有数据可能被覆盖，是否继续？',
      '智能解析确认',
      {
        confirmButtonText: '开始解析',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
  } catch {
    return // 用户取消
  }

  parsing.value = true
  try {
    const res = await api.post('/resumes/parse-attachment', {
      attachmentUrl: form.attachmentUrl
    }, { timeout: 300000 }) // Increased to 5 minutes
    
    if (res.code === 200 && res.data) {
      const data = res.data
      // 填充表单
      if (data.realName) form.realName = data.realName
      if (data.gender) form.gender = data.gender
      if (data.age) form.age = data.age
      if (data.phone) form.phone = data.phone
      if (data.email) form.email = data.email
      if (data.education) form.education = data.education
      if (data.school) form.school = data.school
      if (data.major) form.major = data.major
      if (data.graduationYear) form.graduationYear = data.graduationYear.toString()
      if (data.workYears) form.workYears = data.workYears
      if (data.expectCity) form.expectCity = data.expectCity
      if (data.expectPosition) form.expectPosition = data.expectPosition
      if (data.expectSalaryMin) form.expectSalaryMin = data.expectSalaryMin
      if (data.expectSalaryMax) form.expectSalaryMax = data.expectSalaryMax
      if (data.introduction) form.introduction = data.introduction
      
      // 处理列表数据
      if (data.skills) form.skillList = parseJson(data.skills, [])
      if (data.internshipExperience) form.internshipList = parseJson(data.internshipExperience, [])
      if (data.workExperience) form.workList = parseJson(data.workExperience, [])
      if (data.projectExperience) form.projectList = parseJson(data.projectExperience, [])
      
      ElMessage.success('简历解析成功，请检查并完善信息后保存')
    } else {
      ElMessage.error(res.message || '解析失败')
    }
  } catch (error) {
    console.error('AI parse failed:', error)
    ElMessage.error('解析失败，请检查网络或后端服务')
  } finally {
    parsing.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const data = {
      ...form,
      graduationYear: form.graduationYear ? parseInt(form.graduationYear) : null,
      skills: JSON.stringify(form.skillList),
      internshipExperience: JSON.stringify(form.internshipList),
      workExperience: JSON.stringify(form.workList),
      projectExperience: JSON.stringify(form.projectList)
    }
    // 移除前端临时字段
    delete data.skillList
    delete data.internshipList
    delete data.workList
    delete data.projectList
    
    const res = await api.post('/resumes', data)
    if (res.code === 200) {
      ElMessage.success('简历保存成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('Failed to save resume:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadResume()
})
</script>

<style lang="scss" scoped>
.my-resume-page {
  max-width: 900px;
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

.resume-form {
  padding: 32px;
}

.form-actions {
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
  text-align: center;
  
  .el-button {
    padding: 12px 48px;
  }
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .attachment-card-pro {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    background: linear-gradient(to right, #f8fcfc, #ffffff);
    border: 1px solid #e0f2f2;
    border-radius: 12px;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 190, 189, 0.08);
      border-color: #00bebd;
    }
    
    .file-icon {
      width: 44px;
      height: 44px;
      background: #e6f7f7;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      .el-icon {
        font-size: 24px;
        color: #00bebd;
      }
    }
    
    .file-info {
      flex: 1;
      
      .file-name {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }
      
      .file-meta {
        font-size: 13px;
        color: #999;
      }
    }
    
    .file-actions {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .ai-primary-btn {
        background: linear-gradient(135deg, #00bebd 0%, #00d7d6 100%);
        border: none;
        padding: 8px 20px;
        font-weight: 500;
        box-shadow: 0 4px 10px rgba(0, 190, 189, 0.2);
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 6px 14px rgba(0, 190, 189, 0.3);
        }
      }
    }
  }
}
</style>
