import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

/**
 * WebSocket 服务封装
 * 用于实时消息推送
 */
class WebSocketService {
    constructor() {
        this.stompClient = null
        this.connected = false
        this.subscriptions = new Map()
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectDelay = 3000
    }

    /**
     * 连接 WebSocket
     * @param {string} token - JWT Token
     * @param {function} onConnected - 连接成功回调
     * @param {function} onError - 错误回调
     */
    connect(token, onConnected, onError) {
        if (this.connected) {
            console.log('WebSocket already connected')
            return
        }

        const socket = new SockJS('http://localhost:8081/ws')
        this.stompClient = Stomp.over(socket)

        // 禁用调试日志 (生产环境)
        this.stompClient.debug = null

        const headers = {
            Authorization: `Bearer ${token}`
        }

        this.stompClient.connect(
            headers,
            (frame) => {
                console.log('WebSocket connected:', frame)
                this.connected = true
                this.reconnectAttempts = 0
                if (onConnected) onConnected(frame)
            },
            (error) => {
                console.error('WebSocket error:', error)
                this.connected = false
                if (onError) onError(error)
                this.tryReconnect(token, onConnected, onError)
            }
        )
    }

    /**
     * 尝试重连
     */
    tryReconnect(token, onConnected, onError) {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
            setTimeout(() => {
                this.connect(token, onConnected, onError)
            }, this.reconnectDelay)
        }
    }

    /**
     * 断开连接
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            // 取消所有订阅
            this.subscriptions.forEach((sub) => {
                if (sub.unsubscribe) sub.unsubscribe()
            })
            this.subscriptions.clear()

            this.stompClient.disconnect(() => {
                console.log('WebSocket disconnected')
                this.connected = false
            })
        }
    }

    /**
     * 订阅消息
     * @param {string} destination - 订阅目的地
     * @param {function} callback - 消息回调
     */
    subscribe(destination, callback) {
        if (!this.stompClient || !this.connected) {
            console.error('WebSocket not connected')
            return null
        }

        const subscription = this.stompClient.subscribe(destination, (message) => {
            const payload = JSON.parse(message.body)
            callback(payload)
        })

        this.subscriptions.set(destination, subscription)
        return subscription
    }

    /**
     * 订阅用户私有消息队列
     * @param {number} userId - 用户ID
     * @param {function} callback - 消息回调
     */
    subscribeToUserMessages(userId, callback) {
        // 使用标准 Spring User Destination，自动映射到当前登录用户
        return this.subscribe('/user/queue/messages', callback)
    }

    /**
     * 订阅用户通知
     * @param {number} userId - 用户ID
     * @param {function} callback - 通知回调
     */
    subscribeToNotifications(userId, callback) {
        return this.subscribe('/user/queue/notifications', callback)
    }

    /**
     * 发送消息
     * @param {object} messageDTO - 消息对象 { receiverId, content, attachmentUrl?, messageType? }
     */
    sendMessage(messageDTO) {
        if (!this.stompClient || !this.connected) {
            console.error('WebSocket not connected')
            return false
        }

        this.stompClient.send('/app/chat.send', {}, JSON.stringify(messageDTO))
        return true
    }

    /**
     * 检查是否已连接
     */
    isConnected() {
        return this.connected
    }
}

// 导出单例
export default new WebSocketService()
