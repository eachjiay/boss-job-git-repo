<template>
  <div class="message-list-page">
    <div class="page-container">
      <div class="list-sidebar">
        <div class="list-header">
          <h3>消息列表</h3>
        </div>
        <div class="conversation-list" v-loading="loading">
          <div 
            v-for="conv in conversations" 
            :key="conv.otherSideUserId"
            class="conversation-item"
            :class="{ active: currentConvId === conv.otherSideUserId }"
            @click="selectConversation(conv)"
          >
            <div class="avatar-wrapper">
              <el-avatar :size="48" :src="conv.otherSideAvatar || defaultAvatar" />
              <div class="unread-badge" v-if="conv.unreadCount > 0">{{ conv.unreadCount }}</div>
            </div>
            
            <div class="content-wrapper">
              <div class="top-row">
                <span class="username">{{ conv.otherSideUserName }}</span>
                <span class="time">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="bottom-row">
                <span class="company-job">{{ conv.otherSideCompany }}</span>
                <span class="role-tag">{{ conv.otherSideRole === 'RECRUITER' ? '招聘者' : '求职者' }}</span>
              </div>
              <div class="last-message">{{ conv.lastMessage }}</div>
            </div>
          </div>
          
          <div v-if="!loading && conversations.length === 0" class="empty-list">
            <el-empty description="暂无消息" :image-size="100" />
          </div>
        </div>
      </div>
      
      <div class="chat-container">
        <!-- 始终渲染 router-view，让 Vue Router 决定显示什么 -->
        <router-view v-slot="{ Component }">
          <component :is="Component" v-if="Component" />
          <div v-else class="empty-chat">
            <el-empty description="选择一个联系人开始聊天" />
          </div>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/utils/api'
import { formatTime } from '@/utils'
import { useMessageStore } from '@/stores/message'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const messageStore = useMessageStore()

const loading = ref(false)
const conversations = ref([])
const currentConvId = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

async function loadConversations() {
  loading.value = true
  try {
    const res = await api.get('/messages/conversations')
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load conversations:', error)
  } finally {
    loading.value = false
  }
}

function selectConversation(conv) {
  console.log('Selecting conversation:', conv)
  if (conv && conv.otherSideUserId) {
    const targetPath = `/messages/${conv.otherSideUserId}`
    console.log('Navigating to:', targetPath)
    
    router.push(targetPath).then(() => {
      console.log('Navigation succeeded, current route:', router.currentRoute.value.fullPath)
      // 手动更新 currentConvId 确保高亮正确
      currentConvId.value = conv.otherSideUserId
    }).catch(err => {
      console.error('Navigation failed:', err)
    })
  } else {
    console.error('Invalid conversation data:', conv)
    ElMessage.error('无法打开会话：无效的用户ID')
  }
}

// 监听路由参数变化，用于在子路由刷新时保持激活状态
watch(() => route.params.id, (newId) => {
  if (newId) {
    currentConvId.value = parseInt(newId)
  }
}, { immediate: true })

onMounted(() => {
  loadConversations()
  messageStore.fetchUnreadCount()
  
  // 简易轮询，每30秒刷新一次列表
  setInterval(loadConversations, 30000)
})
</script>

<style lang="scss" scoped>
.message-list-page {
  height: calc(100vh - 60px); // 减去头部高度
  background-color: #f6f6f6;
  padding: 20px 0;
  
  .page-container {
    width: 1000px; // 保持大致宽度与截图一致
    max-width: 100%;
    margin: 0 auto;
    height: 100%;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    display: flex;
    overflow: hidden;
  }
}

.list-sidebar {
  width: 320px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  
  .list-header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      margin: 0;
      font-size: 16px;
      color: #333;
    }
  }
  
  .conversation-list {
    flex: 1;
    overflow-y: auto;
    
    // 隐藏滚动条
    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 3px;
    }
  }
}

.conversation-item {
  display: flex;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  
  &:hover {
    background-color: #f9f9f9;
  }
  
  &.active {
    background-color: #e6f7f7; // 选中态淡青色
  }
  
  .avatar-wrapper {
    position: relative;
    margin-right: 12px;
    flex-shrink: 0;
    
    .unread-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background-color: #f56c6c;
      color: #fff;
      font-size: 12px;
      height: 16px;
      line-height: 16px;
      padding: 0 5px;
      border-radius: 8px;
      border: 1px solid #fff;
    }
  }
  
  .content-wrapper {
    flex: 1;
    overflow: hidden;
    
    .top-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .username {
        font-size: 15px;
        font-weight: 500;
        color: #333;
      }
      
      .time {
        font-size: 12px;
        color: #999;
      }
    }
    
    .bottom-row {
      display: flex;
      align-items: center;
      margin-bottom: 6px;
      font-size: 12px;
      
      .company-job {
        color: #666;
        margin-right: 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 120px;
      }
      
      .role-tag {
        background-color: #f2f2f2;
        color: #999;
        padding: 0 4px;
        border-radius: 2px;
        font-size: 10px;
      }
    }
    
    .last-message {
      font-size: 13px;
      color: #999;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  
  .empty-chat {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
