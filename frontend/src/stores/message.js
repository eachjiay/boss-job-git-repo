import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useMessageStore = defineStore('message', () => {
    const unreadCount = ref(0)

    // 获取未读消息数
    async function fetchUnreadCount() {
        try {
            const res = await api.get('/messages/unread')
            if (res.code === 200) {
                unreadCount.value = res.data
            }
        } catch (error) {
            console.error('Failed to fetch unread count:', error)
        }
    }

    // 发送消息
    async function sendMessage(data) {
        return await api.post('/messages', data)
    }

    return {
        unreadCount,
        fetchUnreadCount,
        sendMessage
    }
})
