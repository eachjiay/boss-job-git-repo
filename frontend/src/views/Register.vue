<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <router-link to="/" class="logo">
          BOSS<span>直聘</span>
        </router-link>
        <h1>注册账号</h1>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="role">
          <div class="role-selector">
            <div 
              class="role-option"
              :class="{ active: form.role === 'SEEKER' }"
              @click="form.role = 'SEEKER'"
            >
              <el-icon><User /></el-icon>
              <span>我要找工作</span>
            </div>
            <div 
              class="role-option"
              :class="{ active: form.role === 'RECRUITER' }"
              @click="form.role = 'RECRUITER'"
            >
              <el-icon><OfficeBuilding /></el-icon>
              <span>我要招人</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名 (3-20位)"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password"
            placeholder="请输入密码 (6-20位)"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱 (选填)"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号 (选填)"
            size="large"
            prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            class="register-btn"
            native-type="submit"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { User, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  role: 'SEEKER'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await userStore.register({
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone,
      role: form.role
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00bebd 0%, #00a5a4 100%);
  padding: 40px 0;
}

.register-container {
  width: 440px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo {
    font-size: 28px;
    font-weight: bold;
    color: #00bebd;
    
    span {
      background: #00bebd;
      color: #fff;
      padding: 2px 8px;
      border-radius: 4px;
      margin-left: 4px;
    }
  }
  
  h1 {
    font-size: 20px;
    color: #333;
    margin-top: 24px;
  }
}

.role-selector {
  display: flex;
  gap: 16px;
  width: 100%;
  
  .role-option {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    
    .el-icon {
      font-size: 32px;
      color: #999;
    }
    
    span {
      font-size: 14px;
      color: #666;
    }
    
    &:hover {
      border-color: #00bebd;
    }
    
    &.active {
      border-color: #00bebd;
      background: #e8f8f8;
      
      .el-icon {
        color: #00bebd;
      }
      
      span {
        color: #00bebd;
        font-weight: 500;
      }
    }
  }
}

.register-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
  }
}

.register-footer {
  text-align: center;
  color: #666;
  
  a {
    color: #00bebd;
    margin-left: 4px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
