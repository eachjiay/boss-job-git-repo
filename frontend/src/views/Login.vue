<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <router-link to="/" class="logo">
          BOSS<span>直聘</span>
        </router-link>
        <h1>账号登录</h1>
      </div>
      
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="邮箱登录" name="email">
          <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" size="large" class="login-form">
            <el-form-item prop="email">
              <el-input
                v-model="emailForm.email"
                placeholder="请输入邮箱地址"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="emailForm.code"
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  maxlength="6"
                  @keyup.enter="handleEmailLogin"
                />
                <el-button 
                  class="send-code-btn" 
                  :disabled="emailCountdown > 0 || !emailForm.email" 
                  @click="sendEmailCode"
                >
                  {{ emailCountdown > 0 ? `${emailCountdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleEmailLogin">
              登录/注册
            </el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="手机登录" name="phone">
          <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" size="large" class="login-form">
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号码"
                prefix-icon="Iphone"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="phoneForm.code"
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  maxlength="6"
                  @keyup.enter="handlePhoneLogin"
                />
                <el-button 
                  class="send-code-btn" 
                  :disabled="countdown > 0 || !phoneForm.phone || phoneForm.phone.length !== 11" 
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handlePhoneLogin">
              登录/注册
            </el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="账号密码登录" name="password">
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名 / 手机号 / 邮箱"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password"
                placeholder="请输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading"
                class="login-btn"
                native-type="submit"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
      
      <el-divider>其他登录方式</el-divider>
      <div class="other-login">
        <el-tooltip content="微信扫码登录" placement="top">
          <div class="wechat-btn" @click="showWechatDialog = true">
            <el-icon :size="28" color="#07c160"><Platform /></el-icon>
          </div>
        </el-tooltip>
      </div>
      
      <div class="demo-accounts">
        <p>演示账号：</p>
        <div class="accounts">
          <el-tag @click="fillDemo('seeker1', '123456')">求职者: seeker1</el-tag>
          <el-tag type="success" @click="fillDemo('hr1', '123456')">招聘者: hr1</el-tag>
        </div>
        <p class="tip">密码均为: 123456</p>
      </div>
    </div>

    <!-- 微信扫码登录模拟弹窗 -->
    <el-dialog
      v-model="showWechatDialog"
      title="微信扫码登录"
      width="400px"
      align-center
      class="wechat-dialog"
    >
      <div class="wechat-qrcode-container">
        <h3>微信登录</h3>
        <p class="subtitle">请使用微信扫描下方二维码登录</p>
        <div class="qrcode-placeholder">
          <el-icon :size="60" color="#07c160"><Link /></el-icon>
          <span>微信开发二维码模拟</span>
        </div>
        
        <el-button 
          type="success" 
          class="simulate-scan-btn" 
          :loading="wechatLoading"
          @click="simulateWechatScan"
        >
          点击模拟：手机扫码并授权成功
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('email') // Default to email login

const formRef = ref()
const loading = ref(false)

// WeChat Dialog states
const showWechatDialog = ref(false)
const wechatLoading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// ==== 手机登录相关 ====
const phoneFormRef = ref(null)
const phoneForm = reactive({
  phone: '',
  code: ''
})
const countdown = ref(0)
let timer = null
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
}

const emailFormRef = ref(null)
const emailForm = reactive({
  email: '',
  code: ''
})
const emailCountdown = ref(0)
let emailTimer = null

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!emailFormRef.value) return
  
  try {
    // 仅校验邮箱字段
    await emailFormRef.value.validateField('email')
    
    // 调用发送API
    await userStore.sendEmailCode({ email: emailForm.email })
    ElMessage.success('验证码已发送至您的邮箱，请注意查收')
    
    // 开启倒计时
    emailCountdown.value = 60
    emailTimer = setInterval(() => {
      emailCountdown.value--
      if (emailCountdown.value <= 0) {
        clearInterval(emailTimer)
      }
    }, 1000)
  } catch (error) {
    if (error.message) {
        ElMessage.error(error.message)
    }
  }
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
}

// 发送手机验证码
const sendCode = async () => {
  if (!phoneFormRef.value) return
  
  try {
    // 仅校验手机号字段
    await phoneFormRef.value.validateField('phone')
    
    // 调用发送API
    await userStore.sendVerificationCode(phoneForm.phone)
    ElMessage.success('验证码发送成功(演示环境默认123456)')
    phoneForm.code = '123456' // 演示环境自动填充
    
    // 开启倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    if (error.message) {
        ElMessage.error(error.message)
    }
  }
}

async function handlePhoneLogin() {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await userStore.loginByPhone(phoneForm.phone, phoneForm.code)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      handleRedirect()
    } else {
      ElMessage.error(res.message || '手机验证码登录失败')
    }
  } catch (error) {
    console.error('Phone login failed:', error)
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await userStore.login(form)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      
      ElMessage.success('登录成功')
      handleRedirect()
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}

function handleRedirect() {
  const redirect = route.query.redirect
  if (redirect) {
    router.push(redirect)
  } else if (userStore.isRecruiter) {
    router.push('/recruiter')
  } else {
    router.push('/')
  }
}

async function handleEmailLogin() {
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.loginByEmail(emailForm)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect)
      } else if (userStore.isRecruiter) {
        router.push('/recruiter')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('Email login failed:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function fillDemo(username, password) {
  activeTab.value = 'password' // Switch to password tab for demo accounts
  form.username = username
  form.password = password
}

// 模拟微信扫码成功流程
async function simulateWechatScan() {
  wechatLoading.value = true
  try {
    // 模拟一个随机的微信 code
    const fakeCode = "sim_wxc_" + Math.random().toString(36).substring(2, 10)
    
    // 调用后端微信登录/注册接口
    const res = await userStore.loginByWechat(fakeCode)
    if (res.code === 200) {
      ElMessage.success('微信授权登录成功！')
      showWechatDialog.value = false
      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect)
      } else if (userStore.isRecruiter) {
        router.push('/recruiter')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(res.message || '微信登录失败')
    }
  } catch (error) {
    console.error('Wechat login failed:', error)
    ElMessage.error(error.message || '微信登录失败')
  } finally {
    wechatLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00bebd 0%, #00a5a4 100%);
}

.login-container {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
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

.login-tabs {
  margin-bottom: 24px;
  :deep(.el-tabs__nav-wrap::after) {
    height: 0;
  }
  :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: bold;
    color: #666;
    &.is-active {
      color: #00bebd;
    }
  }
  :deep(.el-tabs__active-bar) {
    background-color: #00bebd;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
  }
}

.code-input-wrapper {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: center;

  .el-input {
    flex: 1;
  }

  .send-code-btn {
    width: 120px;
    height: 40px;
    padding: 8px 15px;
  }
}

.login-footer {
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

.demo-accounts {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px dashed #e8e8e8;
  text-align: center;
  
  p {
    color: #999;
    font-size: 13px;
    margin-bottom: 12px;
  }
  
  .accounts {
    display: flex;
    justify-content: center;
    gap: 12px;
    
    .el-tag {
      cursor: pointer;
    }
  }
  
  .tip {
    margin-top: 8px;
  }
}

.other-login {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 16px;
  
  .wechat-btn {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background-color: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #e5f7ed;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(7, 193, 96, 0.2);
    }
  }
}

.wechat-qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  
  h3 {
    font-size: 20px;
    color: #333;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: #999;
    margin-bottom: 24px;
  }
  
  .qrcode-placeholder {
    width: 200px;
    height: 200px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: #f9f9f9;
    margin-bottom: 32px;
    
    span {
      margin-top: 16px;
      font-size: 14px;
      color: #666;
    }
  }
  
  .simulate-scan-btn {
    width: 240px;
    height: 44px;
    font-size: 15px;
    border-radius: 22px;
  }
}
</style>
