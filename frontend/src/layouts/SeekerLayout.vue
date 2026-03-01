<template>
  <div class="seeker-layout">
    <header class="header">
      <div class="header-content">
        <router-link to="/" class="logo">
          BOSS<span>直聘</span>
        </router-link>
        
        <nav class="nav-menu">
          <router-link to="/">首页</router-link>
          <router-link to="/jobs">职位搜索</router-link>
          <router-link to="/seeker/resume">我的简历</router-link>
          <router-link to="/seeker/applications">投递记录</router-link>
          <router-link to="/messages">消息</router-link>
          <router-link to="/seeker/favorites">我的收藏</router-link>
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
                <el-dropdown-item command="resume">编辑简历</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <div class="container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

function handleCommand(command) {
  switch (command) {
    case 'resume':
      router.push('/seeker/resume')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
.seeker-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px 0;
  background: #f5f6f7;
}
</style>
