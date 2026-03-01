import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    const isLoggedIn = computed(() => !!token.value)
    const isRecruiter = computed(() => user.value?.role === 'RECRUITER')
    const isSeeker = computed(() => user.value?.role === 'SEEKER')

    function handleLoginSuccess(res) {
        if (res.code === 200) {
            token.value = res.data.token
            user.value = res.data.user
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('user', JSON.stringify(res.data.user))
        }
        return res
    }

    async function login(loginData) {
        const res = await api.post('/auth/login', loginData)
        return handleLoginSuccess(res)
    }

    async function loginByPhone(phone, code) {
        const res = await api.post('/auth/login/phone', { phone, code })
        return handleLoginSuccess(res)
    }

    async function loginByEmail(loginData) {
        // loginData now expects { email, code }
        const res = await api.post('/auth/login/email', loginData)
        return handleLoginSuccess(res)
    }

    async function loginByWechat(code) {
        const res = await api.post('/auth/login/wechat', { code })
        return handleLoginSuccess(res)
    }

    async function sendVerificationCode(phone) {
        const res = await api.post('/auth/send-code', { phone })
        return res
    }

    async function sendEmailCode(emailData) {
        const res = await api.post('/auth/send-email-code', emailData)
        if (res.code === 200) {
            return res.data
        }
        throw new Error(res.message || '获取验证码失败')
    }

    async function register(registerData) {
        const res = await api.post('/auth/register', registerData)
        return res
    }

    async function fetchCurrentUser() {
        if (!token.value) return
        try {
            const res = await api.get('/auth/me')
            if (res.code === 200) {
                user.value = res.data
                localStorage.setItem('user', JSON.stringify(res.data))
            }
        } catch (error) {
            logout()
        }
    }

    function logout() {
        token.value = ''
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    return {
        token, user,
        isLoggedIn, isRecruiter, isSeeker,
        login, loginByPhone, loginByEmail, loginByWechat, sendVerificationCode, sendEmailCode,
        register, fetchCurrentUser, logout
    }
})
