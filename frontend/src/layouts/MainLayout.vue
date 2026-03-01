<template>
  <div class="main-layout">
    <header class="header">
      <div class="header-content">
        <router-link to="/" class="logo">
          BOSS<span>直聘</span>
        </router-link>
        
        <nav class="nav-menu">
          <router-link to="/" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/jobs" :class="{ active: $route.path.startsWith('/jobs') }">职位</router-link>
          <router-link v-if="isLoggedIn" to="/messages" :class="{ active: $route.path.startsWith('/messages') }">消息</router-link>
          <router-link v-if="isRecruiter" to="/recruiter" :class="{ active: $route.path.startsWith('/recruiter') }">招聘端</router-link>
          <router-link v-if="isSeeker" to="/seeker" :class="{ active: $route.path.startsWith('/seeker') }">求职端</router-link>
        </nav>
        
        <div class="header-right">
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="user?.avatar">
                  {{ user?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span>{{ user?.realName || user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="isSeeker" command="resume">我的简历</el-dropdown-item>
                  <el-dropdown-item v-if="isSeeker" command="applications">投递记录</el-dropdown-item>
                  <el-dropdown-item v-if="isRecruiter" command="recruiter">招聘管理</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register" class="login-btn">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <router-view />
    </main>
    
    <footer class="footer" v-if="!isMessagePage">
      <div class="container">
        <p>© 2024 Boss Job 招聘平台 - 让求职更简单</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isMessagePage = computed(() => route.path.startsWith('/messages'))

const user = computed(() => userStore.user)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isRecruiter = computed(() => userStore.isRecruiter)
const isSeeker = computed(() => userStore.isSeeker)

function handleCommand(command) {
  switch (command) {
    case 'resume':
      router.push('/seeker/resume')
      break
    case 'applications':
      router.push('/seeker/applications')
      break
    case 'recruiter':
      router.push('/recruiter')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.footer {
  background: #fff;
  padding: 20px 0;
  text-align: center;
  color: #999;
  border-top: 1px solid #e8e8e8;
}
</style>
