<template>
  <div class="chat-detail">
    <div class="chat-header">
      <div class="user-info">
        <span class="name">{{ receiverName }}</span>
        <span class="status-dot"></span>
      </div>
    </div>
    
    <div class="chat-content" ref="scrollRef">
      <!-- 加载更多按钮 -->
      <div v-if="hasMore" class="load-more">
        <el-button 
          type="text" 
          :loading="loadingMore" 
          @click="loadMoreMessages"
        >
          {{ loadingMore ? '加载中...' : '加载更多消息' }}
        </el-button>
      </div>
      <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ 'is-mine': msg.isMine, 'is-system': msg.messageType === 3 }">
        <el-avatar v-if="msg.messageType !== 3" :size="36" :src="msg.isMine ? myAvatar : receiverAvatar" class="avatar" />
        <div class="bubble-wrapper">
          <!-- 系统消息 (messageType === 3) -->
          <div v-if="msg.messageType === 3" class="system-message">
            <span>{{ msg.content }}</span>
          </div>
          <!-- 简历申请消息 (messageType === 2) -->
          <div v-else-if="msg.messageType === 2" class="message-bubble resume-request">
            <el-icon class="resume-icon"><Document /></el-icon>
            <div class="resume-content">
              <div class="resume-header">
                <span class="type-tag">简历</span>
                <span class="text">{{ msg.content || '求职者发送了一份简历' }}</span>
              </div>
              <div v-if="msg.attachmentUrl" class="attachment-card">
                <el-icon><Paperclip /></el-icon>
                <a :href="msg.attachmentUrl" target="_blank" class="attachment-link">
                  附件简历: {{ getFileName(msg.attachmentUrl) }}
                </a>
              </div>
              <div class="resume-footer">
                <el-button type="primary" size="small" @click="viewResume(msg.senderId)">
                  点击查看在线简历
                </el-button>
              </div>
            </div>
          </div>
          <!-- 普通文本消息 (messageType === 1 或默认) -->
          <div v-else class="message-bubble">
            <!-- 文本内容 -->
            <div v-if="msg.content && msg.content !== '[图片]'">{{ msg.content }}</div>
            <!-- 图片显示 -->
            <div v-if="msg.attachmentUrl && (msg.attachmentUrl.match(/\.(jpg|jpeg|png|gif|webp)$/i) || msg.content === '[图片]')" class="image-bubble">
               <el-image 
                 :src="msg.attachmentUrl" 
                 :preview-src-list="[msg.attachmentUrl]"
                 fit="cover"
                 class="msg-image"
               />
            </div>
            <!-- 附件显示 -->
            <div v-else-if="msg.attachmentUrl" class="attachment-card">
              <el-icon><Paperclip /></el-icon>
              <a :href="msg.attachmentUrl" target="_blank" class="attachment-link">
                {{ getFileName(msg.attachmentUrl) }}
              </a>
            </div>
          </div>
          <div v-if="msg.messageType !== 3" class="message-meta">
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
            <span v-if="msg.isMine" class="read-status" :class="{ 'is-read': msg.isRead === 1 }">
              {{ msg.isRead === 1 ? '已读' : '未读' }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-footer">
      <div class="input-area">
        <el-input
          v-model="inputContent"
          type="textarea"
          :rows="3"
          placeholder="输入新消息..."
          resize="none"
          @keydown="handleKeyDown"
        />
      </div>
      <div class="action-bar">
        <div class="quick-actions">
          <el-button v-if="userStore.isSeeker" type="primary" link @click="handleSendResume">
            <el-icon><Document /></el-icon> 发送简历
          </el-button>
          <el-button v-if="userStore.isRecruiter" type="primary" link @click="handleRequestResume">
            <el-icon><Document /></el-icon> 请求简历
          </el-button>
          
          <el-upload
            class="image-upload"
            action="#"
            :http-request="handleUploadImage"
            :show-file-list="false"
            accept="image/*"
          >
            <el-button type="primary" link :loading="uploadingImage">
              <el-icon><Picture /></el-icon> 发送图片
            </el-button>
          </el-upload>
          
          <el-button type="primary" link @click="handleAudioCall">
             <el-icon><Microphone /></el-icon> 语音通话
          </el-button>
          
          <el-button type="primary" link @click="handleVideoCall">
            <el-icon><VideoCamera /></el-icon> 视频通话
          </el-button>

          <el-button v-if="userStore.isRecruiter" type="warning" link @click="handleAiAssist" :loading="analyzing">
            <el-icon><MagicStick /></el-icon> AI 助手
          </el-button>
        </div>

        <div class="send-actions">
          <el-upload
            class="upload-trigger"
            action="#"
            :http-request="handleUploadFile"
            :show-file-list="false"
            accept=".pdf,.doc,.docx"
          >
            <el-button :icon="Paperclip" circle title="发送附件" :loading="uploading" />
          </el-upload>
          <span class="tip">按 Enter 发送</span>
          <el-button type="primary" size="default" :loading="sending" @click="handleSend">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 视频通话对话框 -->
    <el-dialog
      v-model="videoCallVisible"
      :title="isAudioCall ? '语音通话' : '视频通话'"
      width="360px"
      center
      class="video-call-dialog"
      :close-on-click-modal="false"
    >
      <div class="calling-container">
        <el-avatar :size="80" :src="receiverAvatar" class="calling-avatar" />
        <div class="calling-name">{{ receiverName }}</div>
        <div class="calling-status">正在呼叫...</div>
        <div class="calling-animation">
          <span></span><span></span><span></span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" circle :icon="VideoCamera" @click="videoCallVisible = false" title="挂断" />
        </div>
      </template>
    </el-dialog>

    <!-- AI 分析对话框 -->
    <el-dialog
      v-model="aiVisible"
      title="AI 面试助手"
      width="500px"
      custom-class="ai-analysis-dialog"
    >
      <div v-if="aiAnalysis" class="ai-analysis-content">
        <div class="analysis-section">
          <h3><el-icon><Star /></el-icon> 核心优势</h3>
          <ul class="highlight-list">
            <li v-for="(item, index) in aiAnalysis.highlights" :key="index">{{ item }}</li>
          </ul>
        </div>
        <div class="analysis-section">
          <h3><el-icon><ChatDotRound /></el-icon> 面试建议问题</h3>
          <div class="question-item" v-for="(q, index) in aiAnalysis.interviewQuestions" :key="index">
            <span class="q-num">{{ index + 1 }}.</span>
            <span class="q-text">{{ q }}</span>
            <el-button type="primary" link size="small" @click="sendAiQuestion(q)">发送此问题</el-button>
          </div>
        </div>
      </div>
      <div v-else-if="analyzing" class="ai-loading">
        <el-skeleton :rows="5" animated />
      </div>
    </el-dialog>

    <!-- 视频通话主界面 (全屏浮窗) -->
    <div v-if="callActive" class="video-call-overlay">
      <div class="video-container">
        <!-- 远端视频 -->
        <video v-show="!isAudioCall" ref="remoteVideo" class="remote-video" autoplay playsinline></video>
        <!-- 语音通话封面 -->
        <div v-if="isAudioCall" class="audio-call-active">
            <el-avatar :size="150" :src="receiverAvatar" />
            <div class="audio-status-text">语音通话中...</div>
        </div>

        <!-- 本地视频 (小窗口) -->
        <div v-show="!isAudioCall" class="local-video-wrapper">
          <video ref="localVideo" class="local-video" autoplay playsinline muted></video>
        </div>
        
        <!-- 控制栏 -->
        <div class="call-controls">
          <el-button 
            :type="audioMuted ? 'danger' : 'default'" 
            circle 
            @click="toggleAudio"
            :icon="Microphone"
          />
          <el-button 
            type="danger" 
            circle 
            class="hangup-btn"
            @click="handleHangup"
            :icon="Phone"
          />
          <el-button 
            v-if="!isAudioCall"
            :type="videoMuted ? 'danger' : 'default'" 
            circle 
            @click="toggleVideo"
            :icon="Camera"
          />
        </div>

        <div class="user-info-overlay">
          <div class="name">{{ receiverName }}</div>
          <div class="status">{{ callTimer }}</div>
        </div>
      </div>
    </div>

    <!-- 来电提醒 -->
    <el-dialog
      v-model="incomingCallVisible"
      :title="isAudioCall ? '语音通话邀请' : '视频通话邀请'"
      width="300px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="incoming-call-info">
        <el-avatar :size="64" :src="callerInfo.avatar || receiverAvatar" />
        <div class="name">{{ callerInfo.name || receiverName }}</div>
        <div class="text">{{ isAudioCall ? '邀请您进行语音通话...' : '邀请您进行视频通话...' }}</div>
      </div>
      <template #footer>
        <div class="incoming-footer">
          <el-button type="danger" circle :icon="Close" @click="handleRejectCall" />
          <el-button type="success" circle :icon="Check" @click="handleAcceptCall" />
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import api from '@/utils/api'
import { formatTime } from '@/utils'
import websocketService from '@/utils/websocket'
import { Paperclip, Document, Picture, VideoCamera, MagicStick, ChatDotRound, Microphone, Phone, Camera, Close, Check, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

function viewResume(userId) {
  router.push(`/resumes/${userId}`)
}

const receiverId = ref(route.params.id)
const messages = ref([])
const inputContent = ref('')
const sending = ref(false)
const uploading = ref(false)
const scrollRef = ref(null)
const uploadingImage = ref(false)
const aiVisible = ref(false)
const analyzing = ref(false)
const aiAnalysis = ref(null)
const videoCallVisible = ref(false)

// WebRTC & Call State
const localVideo = ref(null)
const remoteVideo = ref(null)
const localStream = ref(null)
const peerConnection = ref(null)
const callActive = ref(false)
const incomingCallVisible = ref(false)
const audioMuted = ref(false)
const videoMuted = ref(false)
const callTimer = ref('00:00')
const callerInfo = ref({ name: '', avatar: '', id: null })
let timerInterval = null
let startTime = 0

// ICE 服务器配置
const rtcConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
}

// 呼叫超时计时器
let callTimeoutTimer = null

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
const totalMessages = ref(0)
const hasMore = ref(false)
const loadingMore = ref(false)

const myAvatar = userStore.user?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const receiverName = ref('正在加载...')
const receiverAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

async function loadHistory() {
  if (!receiverId.value) return
  
  try {
    // 使用分页API加载最新消息
    const res = await api.get(`/messages/${receiverId.value}/paged`, {
      params: { page: 1, size: pageSize.value }
    })
    if (res.code === 200) {
      messages.value = res.data.records || []
      totalMessages.value = res.data.total || 0
      currentPage.value = 1
      hasMore.value = res.data.hasMore || false
      scrollToBottom()
      
      // 尝试从第一条对方的消息中获取头像和昵称
      const otherMsg = messages.value.find(m => !m.isMine)
      if (otherMsg) {
        receiverName.value = otherMsg.senderName
        receiverAvatar.value = otherMsg.senderAvatar || receiverAvatar.value
      } else {
        receiverName.value = '对方'
      }
    }
  } catch (error) {
    console.error('Failed to load history:', error)
  }
}

async function loadMoreMessages() {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  try {
    const nextPage = currentPage.value + 1
    const res = await api.get(`/messages/${receiverId.value}/paged`, {
      params: { page: nextPage, size: pageSize.value }
    })
    if (res.code === 200 && res.data.records?.length) {
      // 将旧消息插入到开头
      messages.value = [...res.data.records, ...messages.value]
      currentPage.value = nextPage
      hasMore.value = res.data.hasMore || false
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('Failed to load more messages:', error)
  } finally {
    loadingMore.value = false
  }
}

async function handleSend() {
  const content = inputContent.value.trim()
  if (!content) return
  
  sending.value = true
  try {
    const res = await messageStore.sendMessage({
      receiverId: receiverId.value,
      content: content
    })
    
    if (res.code === 200) {
      // 乐观更新 UI
      messages.value.push(res.data)
      inputContent.value = ''
      scrollToBottom()
    }
  } catch (error) {
    console.error('Failed to send message:', error)
    sending.value = false
  }
}

function handleKeyDown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  })
}

function getFileName(url) {
  if (!url) return '附件'
  return url.substring(url.lastIndexOf('/') + 1)
}

async function handleUploadFile(options) {
  const { file } = options
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const uploadRes = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (uploadRes.code === 200) {
      const attachmentUrl = uploadRes.data
      const res = await messageStore.sendMessage({
        receiverId: receiverId.value,
        content: `[附件] ${file.name}`,
        attachmentUrl: attachmentUrl,
        messageType: 1 // 普通附件
      })
      
      if (res.code === 200) {
        messages.value.push(res.data)
        scrollToBottom()
        ElMessage.success('附件发送成功')
      }
    }
  } catch (error) {
    console.error('Upload failed:', error)
    ElMessage.error('发送附件失败')
  } finally {
    uploading.value = false
  }
}

async function handleUploadImage(options) {
  const { file } = options
  uploadingImage.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const uploadRes = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (uploadRes.code === 200) {
      const attachmentUrl = uploadRes.data
      const res = await messageStore.sendMessage({
        receiverId: receiverId.value,
        content: '[图片]',
        attachmentUrl: attachmentUrl,
        messageType: 1 // 这里可以先用1，或者将来用新的4
      })
      
      if (res.code === 200) {
        messages.value.push(res.data)
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('Image upload failed:', error)
    ElMessage.error('图片发送失败')
  } finally {
    uploadingImage.value = false
  }
}

async function handleSendResume() {
  try {
    await ElMessageBox.confirm('发送您的在线简历给对方？', '确认发送', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await messageStore.sendMessage({
      receiverId: receiverId.value,
      content: '我向您发送了一份简历，请查收',
      messageType: 2 // 简历类型
    })
    
    if (res.code === 200) {
      messages.value.push(res.data)
      scrollToBottom()
      ElMessage.success('简历已发送')
    }
  } catch (e) {
    // 用户取消
  }
}

async function handleRequestResume() {
  const res = await messageStore.sendMessage({
    receiverId: receiverId.value,
    content: '您好，我对您的简历很感兴趣，可以发一份简历给我吗？',
    messageType: 1
  })
  
  if (res.code === 200) {
    messages.value.push(res.data)
    scrollToBottom()
    ElMessage.success('已请求简历')
  }
}

// --- 视频通话逻辑 ---

// --- 视频通话逻辑 ---

// 获取媒体流（带降级处理）
// 获取媒体流（带降级处理）
async function getUserMediaSafe(constraints = { video: true, audio: true }) {
  try {
    // 如果显式请求禁用视频，直接请求音频
    if (constraints.video === false) {
      if (typeof videoMuted !== 'undefined') videoMuted.value = true
      return await navigator.mediaDevices.getUserMedia({ video: false, audio: true })
    }
    return await navigator.mediaDevices.getUserMedia(constraints)
  } catch (err) {
    console.warn('Media access error:', err)
    // 常见错误：NotReadableError (设备被占用), TrackStartError (无法启动), or specific message
    const isDeviceBusy = err.name === 'NotReadableError' || 
                         err.name === 'TrackStartError' || 
                         err.message?.includes('Device in use') || 
                         err.message?.includes('Could not start video source')
    
    if (isDeviceBusy) {
      ElMessage.warning('摄像头被占用，已切换为仅语音模式')
      if (typeof videoMuted !== 'undefined') {
          videoMuted.value = true // 自动标记为视频静音
      }
      return await navigator.mediaDevices.getUserMedia({ video: false, audio: true })
    }
    throw err
  }
}

const isAudioCall = ref(false) // 标记当次通话是否为语音通话

async function handleAudioCall() {
  if (!websocketService.isConnected()) {
    ElMessage.error('WebSocket 未连接，无法发起通话，请刷新页面重试')
    return
  }
  isAudioCall.value = true
  await startCall('INVITE_AUDIO')
}

// 复用发起逻辑
async function startCall(inviteType = 'INVITE') {
   console.log('Starting call type:', inviteType)
   try {
     // 1. 获取本地流
     const constraints = (inviteType === 'INVITE_AUDIO') ? { video: false, audio: true } : { video: true, audio: true }
     localStream.value = await getUserMediaSafe(constraints)
     
     console.log('Got local stream:', localStream.value.id)
     videoCallVisible.value = true 
     
     // 2. 发送呼叫邀请
     websocketService.sendMessage({
       receiverId: receiverId.value,
       messageType: 4, 
       content: inviteType
     })
     
     // 设置 30秒 超时
     callTimeoutTimer = setTimeout(() => {
       if (videoCallVisible.value && !callActive.value) {
         videoCallVisible.value = false
         stopStream()
         ElMessage.warning('对方无人接听')
         websocketService.sendMessage({
           receiverId: receiverId.value,
           messageType: 6, 
           content: 'CANCEL' 
         })
       }
     }, 30000)
 
   } catch (err) {
     console.error('Media error:', err)
     ElMessage.error('无法获取麦克风/摄像头权限')
   }
}

async function handleVideoCall() {
  isAudioCall.value = false
  await startCall('INVITE')
}

async function handleAcceptCall() {
  incomingCallVisible.value = false
  try {
    const constraints = isAudioCall.value ? { video: false, audio: true } : { video: true, audio: true }
    localStream.value = await getUserMediaSafe(constraints)
    callActive.value = true
    
    // 初始化 PeerConnection
    initPeerConnection()
    
    // 发送接受信令给呼叫方
    websocketService.sendMessage({
      receiverId: callerInfo.value.id,
      messageType: 5, // CALL_ACCEPT
      content: isAudioCall.value ? 'ACCEPT_AUDIO' : 'ACCEPT_VIDEO'
    })
    
    startTimer()
    
    // 等待本地视频渲染后赋值
    nextTick(() => {
      if (localVideo.value) {
        localVideo.value.srcObject = localStream.value
      }
    })
  } catch (err) {
    console.error('Accept call failed:', err)
    ElMessage.error('接听失败: ' + err.message)
    handleRejectCall()
  }
}

function handleRejectCall() {
  incomingCallVisible.value = false
  websocketService.sendMessage({
    receiverId: callerInfo.value.id,
    messageType: 6, // CALL_REJECT
    content: 'REJECT'
  })
}

function handleHangup() {
  stopStream()
  closePeerConnection()
  callActive.value = false
  videoCallVisible.value = false
  incomingCallVisible.value = false
  
  // 发送挂断信号
  const targetId = callerInfo.value.id || receiverId.value
  if(targetId) {
    websocketService.sendMessage({
      receiverId: targetId,
      messageType: 6, // CALL_REJECT / HANGUP
      content: 'HANGUP'
    })
  }
  
  if (callTimeoutTimer) {
    clearTimeout(callTimeoutTimer)
    callTimeoutTimer = null
  }
}

// 初始化 WebRTC
function initPeerConnection() {
  if (peerConnection.value) {
    peerConnection.value.close()
  }
  
  peerConnection.value = new RTCPeerConnection(rtcConfig)
  
  // 添加轨道
  localStream.value.getTracks().forEach(track => {
    peerConnection.value.addTrack(track, localStream.value)
  })
  
  // 监听远端轨道
  peerConnection.value.ontrack = (event) => {
    if (remoteVideo.value) {
      remoteVideo.value.srcObject = event.streams[0]
    }
  }
  
  // 监听 ICE 候选人
  peerConnection.value.onicecandidate = (event) => {
    if (event.candidate) {
      console.log('Sending ICE candidate')
      websocketService.sendMessage({
        receiverId: callerInfo.value.id || receiverId.value,
        messageType: 7, // CALL_SIGNALING
        content: JSON.stringify({ type: 'candidate', candidate: event.candidate })
      })
    }
  }
}

async function createOffer() {
  try {
    const offer = await peerConnection.value.createOffer()
    await peerConnection.value.setLocalDescription(offer)
    websocketService.sendMessage({
      receiverId: callerInfo.value.id || receiverId.value,
      messageType: 7, // CALL_SIGNALING
      content: JSON.stringify({ type: 'offer', offer })
    })
  } catch (err) {
    console.error('Create offer failed:', err)
  }
}

async function handleOffer(offer) {
  try {
    if (!peerConnection.value) initPeerConnection()
    await peerConnection.value.setRemoteDescription(new RTCSessionDescription(offer))
    const answer = await peerConnection.value.createAnswer()
    await peerConnection.value.setLocalDescription(answer)
    websocketService.sendMessage({
      receiverId: callerInfo.value.id || receiverId.value,
      messageType: 7, // CALL_SIGNALING
      content: JSON.stringify({ type: 'answer', answer })
    })
  } catch (err) {
    console.error('Handle offer failed:', err)
  }
}

async function handleAnswer(answer) {
  try {
    await peerConnection.value.setRemoteDescription(new RTCSessionDescription(answer))
  } catch (err) {
    console.error('Handle answer failed:', err)
  }
}

async function handleCandidate(candidate) {
  try {
    if (peerConnection.value) {
      await peerConnection.value.addIceCandidate(new RTCIceCandidate(candidate))
    }
  } catch (e) {
    console.warn('Error adding candidate', e)
  }
}

function stopStream() {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
    localStream.value = null
  }
}

function closePeerConnection() {
  if (peerConnection.value) {
    peerConnection.value.close()
    peerConnection.value = null
  }
  stopTimer()
}

function startTimer() {
  startTime = Date.now()
  timerInterval = setInterval(() => {
    const totalSeconds = Math.floor((Date.now() - startTime) / 1000)
    const m = Math.floor(totalSeconds / 60).toString().padStart(2, '0')
    const s = (totalSeconds % 60).toString().padStart(2, '0')
    callTimer.value = `${m}:${s}`
  }, 1000)
}

function stopTimer() {
  if (timerInterval) clearInterval(timerInterval)
  callTimer.value = '00:00'
}

function toggleAudio() {
  audioMuted.value = !audioMuted.value
  if (localStream.value) {
    const audioTracks = localStream.value.getAudioTracks()
    if (audioTracks.length > 0) {
      audioTracks[0].enabled = !audioMuted.value
    }
  }
}

function toggleVideo() {
  videoMuted.value = !videoMuted.value
  if (localStream.value) {
    const videoTracks = localStream.value.getVideoTracks()
    if (videoTracks.length > 0) {
      videoTracks[0].enabled = !videoMuted.value
    } else {
       ElMessage.warning('当前为语音模式，无视频画面')
       videoMuted.value = true 
    }
  }
}

// 处理通话相关信令
async function handleSignalingMessage(message) {
  const type = message.messageType
  const data = message.content
  console.log('Received signaling message:', type, data, 'from:', message.senderId)

  switch (type) {
    case 4: // INVITE
      // 如果正在通话中或正在呼叫别人，自动拒绝并告知忙线
      if (callActive.value || videoCallVisible.value || incomingCallVisible.value) {
         websocketService.sendMessage({
           receiverId: message.senderId,
           messageType: 6, // REJECT
           content: 'BUSY'
         })
         return
      }

      // Check content for call type
      isAudioCall.value = (message.content === 'INVITE_AUDIO')

      callerInfo.value = {
        name: message.senderName,
        avatar: message.senderAvatar,
        id: message.senderId
      }
      incomingCallVisible.value = true
      break
    case 5: // ACCEPT
      videoCallVisible.value = false
      callActive.value = true
      // 如果我之前没有设置过 callerInfo（作为呼叫方），这里需要补全
      if (!callerInfo.value.id) {
        callerInfo.value = {
          name: receiverName.value,
          avatar: receiverAvatar.value,
          id: receiverId.value
        }
      }
      
      if (callTimeoutTimer) {
        clearTimeout(callTimeoutTimer)
        callTimeoutTimer = null
      }
      initPeerConnection()
      createOffer() // 呼叫方主动发起 Offer
      startTimer()
      nextTick(() => {
        if (localVideo.value) localVideo.value.srcObject = localStream.value
      })
      break
    case 6: // REJECT / HANGUP
      if (data === 'BUSY') {
        ElMessage.warning('对方正在忙线中')
      } else if (data === 'CANCEL') {
        ElMessage.info('对方已取消呼叫')
      } else {
        ElMessage.info('通话已结束/已拒绝')
      }
      
      stopStream()
      closePeerConnection()
      callActive.value = false
      videoCallVisible.value = false
      incomingCallVisible.value = false
      callerInfo.value = { name: '', avatar: '', id: null }
      
      if (callTimeoutTimer) {
        clearTimeout(callTimeoutTimer)
        callTimeoutTimer = null
      }
      break
    case 7: // SIGNALING (ICE/SDP)
      if (!callActive.value) return
      const signal = JSON.parse(data)
      if (signal.type === 'offer') handleOffer(signal.offer)
      else if (signal.type === 'answer') handleAnswer(signal.answer)
      else if (signal.type === 'candidate') handleCandidate(signal.candidate)
      break
  }
}

// --- AI 助手逻辑 ---

async function handleAiAssist() {
  aiVisible.value = true
  analyzing.value = true
  aiAnalysis.value = null
  try {
    const res = await api.get(`/resumes/ai-assist/${receiverId.value}`)
    if (res.code === 200) {
      if (typeof res.data === 'string') {
        aiAnalysis.value = JSON.parse(res.data)
      } else {
        aiAnalysis.value = res.data
      }
    }
  } catch (error) {
    console.error('AI Analysis failed:', error)
    ElMessage.error('AI 分析失败')
  } finally {
    analyzing.value = false
  }
}

function sendAiQuestion(q) {
  inputContent.value = q
  aiVisible.value = false
  handleSend()
}

// 监听路由ID变化，重新加载
watch(() => route.params.id, (newId) => {
  if (newId) {
    receiverId.value = newId
    loadHistory()
  }
})

// WebSocket 回调：接收到新消息
function handleIncomingMessage(message) {
  // 处理通话信令 (type >= 4)
  if (message.messageType >= 4) {
    handleSignalingMessage(message)
    return
  }
  // 只处理当前聊天对象的消息
  const isFromCurrentChat = 
    (message.senderId == receiverId.value) || 
    (message.receiverId == receiverId.value)
  
  if (isFromCurrentChat) {
    // 避免重复添加（如果是自己发的消息，前端可能已经乐观更新了）
    const exists = messages.value.some(m => m.id === message.id)
    if (!exists) {
      messages.value.push(message)
      scrollToBottom()
    }
  }
}

onMounted(() => {
  loadHistory()
  connectWebSocket()
})

// 连接 WebSocket
function connectWebSocket() {
  const token = localStorage.getItem('token')
  const userId = userStore.user?.id
  
  if (token && userId && !websocketService.isConnected()) {
    websocketService.connect(
      token,
      () => {
        // 连接成功，订阅用户消息队列
        websocketService.subscribeToUserMessages(userId, handleIncomingMessage)
      },
      (error) => {
        console.error('WebSocket connection error:', error)
      }
    )
  }
}

const subscription = ref(null)

onUnmounted(() => {
  stopStream()
  closePeerConnection()
  if (subscription.value) {
    subscription.value.unsubscribe()
  }
})
</script>

<style lang="scss" scoped>
.chat-detail {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  
  .name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  
  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    background-color: #67c23a; // 在线状态
    border-radius: 50%;
    margin-left: 8px;
  }
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fff;
  
  .load-more {
    text-align: center;
    padding: 10px 0 20px;
    
    .el-button {
      color: #999;
      font-size: 13px;
      
      &:hover {
        color: #00bebd;
      }
    }
  }
  
  .message-item {
    display: flex;
    margin-bottom: 20px;
    
    .avatar {
      flex-shrink: 0;
      margin-right: 12px;
    }
    
    .bubble-wrapper {
      max-width: 70%;
      
      .message-bubble {
        background-color: #f4f4f5;
        color: #333;
        padding: 10px 16px;
        border-radius: 8px;
        border-top-left-radius: 2px;
        font-size: 14px;
        line-height: 1.5;
        white-space: pre-wrap;
        word-break: break-all;
      }
      .message-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 4px;
        
        .message-time {
          font-size: 12px;
          color: #999;
        }
        
        .read-status {
          font-size: 11px;
          color: #999;
          
          &.is-read {
            color: #00bebd;
          }
        }
      }
    }
    
    &.is-mine {
      flex-direction: row-reverse;
      
      .avatar {
        margin-right: 0;
        margin-left: 12px;
      }
      
      .bubble-wrapper {
        text-align: right;
        
        .message-bubble {
          background-color: #00bebd; // 主题色
          color: #fff;
          border-radius: 8px;
          border-top-right-radius: 2px;
          text-align: left;
        }
      }
    }
  }
}

.chat-footer {
  border-top: 1px solid #f0f0f0;
  padding: 16px 24px;
  
  .input-area {
    margin-bottom: 12px;
  }
  
  :deep(.el-textarea__inner) {
    border: none;
    padding: 0;
    resize: none;
    box-shadow: none; // 去掉聚焦时的阴影
    
    &:hover, &:focus {
      box-shadow: none;
    }
  }
  
  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f5f5f5;
    padding-top: 12px;
    
    .quick-actions {
      display: flex;
      gap: 16px;
      
      .el-button {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #666;
        padding: 0;
        
        &:hover {
          color: #00bebd;
        }
        
        .el-icon {
          font-size: 16px;
        }
      }
    }

    .send-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .tip {
      font-size: 12px;
      color: #999;
    }
  }
}

// 系统消息样式
.message-item.is-system {
  justify-content: center;
  margin: 16px 0;
  
  .bubble-wrapper {
    max-width: 100%;
    text-align: center;
  }
  
  .system-message {
    display: inline-block;
    padding: 6px 16px;
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 16px;
    font-size: 12px;
    color: #999;
  }
}

// 简历申请消息样式
.resume-request {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background-color: #fff7e6 !important;
  border: 1px solid #ffd666;
  
  .resume-icon {
    font-size: 20px;
    color: #fa8c16;
    flex-shrink: 0;
    margin-top: 2px;
  }
  
  .resume-content {
    flex: 1;
    
    .resume-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      
      .type-tag {
        background-color: #fa8c16;
        color: #fff;
        font-size: 11px;
        padding: 2px 6px;
        border-radius: 4px;
      }
      
      .text {
        font-weight: 500;
        font-size: 14px;
      }
    }
    
    .attachment-card {
      margin-bottom: 12px;
    }
    
    .resume-footer {
      border-top: 1px solid rgba(0, 0, 0, 0.05);
      padding-top: 10px;
    }
  }
}

.image-bubble {
  max-width: 240px;
  border-radius: 4px;
  overflow: hidden;
  
  .msg-image {
    width: 100%;
    display: block;
    cursor: pointer;
  }
}

// 视频通话对话框样式
.video-call-dialog {
  :deep(.el-dialog__body) {
    padding-top: 10px;
    padding-bottom: 30px;
  }
}

.calling-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .calling-avatar {
    margin-bottom: 16px;
    border: 4px solid #f0f0f0;
  }
  
  .calling-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  .calling-status {
    color: #999;
    font-size: 14px;
    margin-bottom: 24px;
  }
}

.calling-animation {
  display: flex;
  gap: 8px;
  
  span {
    width: 8px;
    height: 8px;
    background-color: #00bebd;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1.0); }
}

.dialog-footer {
  .el-button {
    font-size: 24px;
    padding: 18px;
  }
}

// AI 助手样式
.ai-analysis-content {
  .analysis-section {
    margin-bottom: 24px;
    
    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      margin-bottom: 12px;
      color: #333;
      
      .el-icon {
        color: #e6a23c;
      }
    }
  }
  
  .highlight-list {
    margin: 0;
    padding-left: 20px;
    color: #606266;
    line-height: 1.8;
  }
  
  .question-item {
    background: #f8f9fa;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .q-num {
      font-weight: bold;
      color: #00bebd;
    }
    
    .q-text {
      color: #333;
      line-height: 1.5;
    }
    
    .el-button {
      align-self: flex-end;
      padding: 0;
    }
  }
}

.ai-loading {
  padding: 20px 0;
}

// 视频通话主界面样式
.video-call-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #000;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .video-container {
    position: relative;
    width: 100vw;
    height: 100vh;
    background: #1a1a1a;
    
    .remote-video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .audio-call-active {
        position: absolute;
        top: 40%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        z-index: 10;
        
        .el-avatar {
          border: 4px solid rgba(255,255,255,0.2);
          box-shadow: 0 0 30px rgba(0,0,0,0.5);
          animation: pulse 2s infinite;
        }
        
        .audio-status-text {
          margin-top: 24px;
          font-size: 20px;
          color: #fff;
          font-weight: 500;
          text-shadow: 0 2px 4px rgba(0,0,0,0.5);
        }
      }
      
      @keyframes pulse {
        0% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.4); }
        70% { box-shadow: 0 0 0 20px rgba(255, 255, 255, 0); }
        100% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0); }
      }

    .local-video-wrapper {
      position: absolute;
      top: 24px;
      right: 24px;
      width: 160px;
      height: 240px;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5);
      background: #000;
      border: 2px solid rgba(255,255,255,0.2);
      
      .local-video {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .call-controls {
      position: absolute;
      bottom: 40px;
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      gap: 32px;
      align-items: center;
      
      .el-button {
        font-size: 24px;
        width: 64px;
        height: 64px;
        background: rgba(255,255,255,0.15);
        border: none;
        backdrop-filter: blur(10px);
        color: #fff;
        
        &:hover {
          background: rgba(255,255,255,0.25);
        }
        
        &.hangup-btn {
          background: #f56c6c;
          width: 72px;
          height: 72px;
          
          &:hover { background: #f78989; }
        }
        
        &.el-button--danger {
          background: #f56c6c;
        }
      }
    }
    
    .user-info-overlay {
      position: absolute;
      top: 40px;
      left: 40px;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0,0,0,0.5);
      
      .name {
        font-size: 28px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      .status {
        font-size: 18px;
        opacity: 0.8;
      }
    }
  }
}

.incoming-call-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
  
  .name {
    margin-top: 16px;
    font-size: 18px;
    font-weight: 600;
  }
  
  .text {
    margin-top: 8px;
    color: #999;
  }
}

.incoming-footer {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-bottom: 20px;
  
  .el-button {
    font-size: 24px;
    width: 60px;
    height: 60px;
  }
}

</style>
