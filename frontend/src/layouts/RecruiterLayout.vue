<template>
  <div class="recruiter-layout">
    <header class="header">
      <div class="header-content">
        <router-link to="/" class="logo">
          BOSS<span>直聘</span>
        </router-link>
        
        <nav class="nav-menu">
          <router-link to="/recruiter/candidates">人才库</router-link>
          <router-link to="/recruiter/applications">牛人管理</router-link>
          <router-link to="/messages">消息</router-link>
          <router-link to="/recruiter/jobs">职位管理</router-link>
          <router-link to="/recruiter/company">公司信息</router-link>
        </nav>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32">
                {{ user?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span>{{ user?.realName || user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回首页</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <div class="layout-body">
      <aside class="sidebar">
        <div class="sidebar-menu">
          <router-link to="/recruiter/candidates" class="menu-item" :class="{ active: $route.path === '/recruiter/candidates' }">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </router-link>
          <router-link to="/recruiter/applications" class="menu-item" :class="{ active: $route.path === '/recruiter/applications' }">
            <el-icon><User /></el-icon>
            <span>牛人管理</span>
          </router-link>
          <router-link to="/messages" class="menu-item" :class="{ active: $route.path.startsWith('/messages') }">
            <el-icon><ChatDotRound /></el-icon>
            <span>消息</span>
          </router-link>
          <router-link to="/recruiter/jobs" class="menu-item" :class="{ active: $route.path === '/recruiter/jobs' }">
            <el-icon><Document /></el-icon>
            <span>职位管理</span>
          </router-link>
          <router-link to="/recruiter/post-job" class="menu-item" :class="{ active: $route.path === '/recruiter/post-job' }">
            <el-icon><Plus /></el-icon>
            <span>发布职位</span>
          </router-link>
          <router-link to="/recruiter/company" class="menu-item" :class="{ active: $route.path === '/recruiter/company' }">
            <el-icon><OfficeBuilding /></el-icon>
            <span>公司信息</span>
          </router-link>
        </div>
      </aside>
      
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, Search, User, Document, Plus, OfficeBuilding, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

function handleCommand(command) {
  switch (command) {
    case 'home':
      router.push('/')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
.recruiter-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-body {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  padding: 20px;
  background: #f5f6f7;
  overflow-y: auto;
}
</style>
