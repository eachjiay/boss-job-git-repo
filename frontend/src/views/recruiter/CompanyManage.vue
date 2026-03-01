<template>
  <div class="company-manage-page form-page">
    <div class="card">
      <div class="page-header">
        <h1>公司信息</h1>
        <p>完善公司信息，吸引更多优秀人才</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        v-loading="loading"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="公司名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入公司全称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="form.industry" placeholder="选择行业" style="width: 100%;">
                <el-option v-for="ind in industryList" :key="ind" :label="ind" :value="ind" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="公司规模" prop="scale">
              <el-select v-model="form.scale" placeholder="选择规模" style="width: 100%;">
                <el-option v-for="s in scaleList" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="融资阶段" prop="financing">
              <el-select v-model="form.financing" placeholder="选择融资阶段" style="width: 100%;">
                <el-option v-for="f in financingList" :key="f" :label="f" :value="f" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="所在城市" prop="city">
              <el-select v-model="form.city" placeholder="选择城市" style="width: 100%;">
                <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="公司详细地址" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="公司简介" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="简要介绍公司业务、发展历程等"
          />
        </el-form-item>
        
        <el-form-item label="公司福利">
          <el-checkbox-group v-model="form.benefitList">
            <el-checkbox v-for="b in benefitOptions" :key="b" :label="b" :value="b" />
          </el-checkbox-group>
        </el-form-item>
        
        <div class="form-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            保存信息
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { cityList, scaleList, financingList } from '@/utils'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const companyId = ref(null)

const form = reactive({
  name: '',
  industry: '',
  scale: '',
  financing: '',
  city: '',
  address: '',
  description: '',
  benefitList: []
})

const rules = {
  name: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请选择行业', trigger: 'change' }],
  scale: [{ required: true, message: '请选择公司规模', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }]
}

const industryList = [
  '互联网', '电子商务', '金融', '教育', '医疗健康', '房地产', '制造业',
  '游戏', '人工智能', '新能源', '物流', '文化传媒', '咨询', '其他'
]

const benefitOptions = [
  '五险一金', '带薪年假', '弹性工作', '免费三餐', '定期体检',
  '股票期权', '年终奖金', '节日福利', '交通补贴', '住房补贴', '健身房'
]

async function loadCompany() {
  loading.value = true
  try {
    // 这里需要一个获取当前用户公司的接口
    // 暂时使用 ID 为 1 的公司作为示例
    const res = await api.get('/companies/1')
    if (res.code === 200 && res.data) {
      companyId.value = res.data.id
      Object.assign(form, {
        name: res.data.name || '',
        industry: res.data.industry || '',
        scale: res.data.scale || '',
        financing: res.data.financing || '',
        city: res.data.city || '',
        address: res.data.address || '',
        description: res.data.description || '',
        benefitList: res.data.benefits || []
      })
    }
  } catch (error) {
    // 公司不存在，显示空表单
    console.log('No company found, show empty form')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const data = {
      ...form,
      userId: userStore.user?.id,
      benefits: JSON.stringify(form.benefitList)
    }
    delete data.benefitList
    
    let res
    if (companyId.value) {
      res = await api.put(`/companies/${companyId.value}`, data)
    } else {
      res = await api.post('/companies', data)
    }
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      if (!companyId.value) {
        companyId.value = res.data.id
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('Failed to save company:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCompany()
})
</script>

<style lang="scss" scoped>
.company-manage-page {
  padding: 20px;
  max-width: 900px;
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

.form-actions {
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}
</style>
