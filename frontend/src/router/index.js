import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/',
        component: () => import('@/layouts/MainLayout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/Home.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'jobs',
                name: 'JobSearch',
                component: () => import('@/views/JobSearch.vue'),
                meta: { title: '职位搜索' }
            },
            {
                path: 'jobs/:id',
                name: 'JobDetail',
                component: () => import('@/views/JobDetail.vue'),
                meta: { title: '职位详情' }
            },
            {
                path: 'companies/:id',
                name: 'CompanyDetail',
                component: () => import('@/views/CompanyDetail.vue'),
                meta: { title: '公司详情' }
            },
            {
                path: 'messages',
                component: () => import('../views/MessageList.vue'),
                meta: { requiresAuth: true },
                children: [
                    {
                        path: ':id',
                        name: 'ChatDetail',
                        component: () => import('../views/ChatDetail.vue'),
                        meta: { requiresAuth: true }
                    }
                ]
            }
        ]
    },

    {
        path: '/recruiter',
        component: () => import('@/layouts/RecruiterLayout.vue'),
        meta: { requiresAuth: true, role: 'RECRUITER' },
        children: [
            {
                path: '',
                redirect: '/recruiter/candidates'
            },
            {
                path: 'candidates',
                name: 'CandidateSearch',
                component: () => import('@/views/recruiter/CandidateSearch.vue'),
                meta: { title: '人才搜索' }
            },
            {
                path: 'applications',
                name: 'ReceivedApplications',
                component: () => import('@/views/recruiter/ReceivedApplications.vue'),
                meta: { title: '牛人管理' }
            },
            {
                path: 'jobs',
                name: 'MyJobs',
                component: () => import('@/views/recruiter/MyJobs.vue'),
                meta: { title: '职位管理' }
            },
            {
                path: 'post-job',
                name: 'PostJob',
                component: () => import('@/views/recruiter/PostJob.vue'),
                meta: { title: '发布职位' }
            },
            {
                path: 'company',
                name: 'CompanyManage',
                component: () => import('@/views/recruiter/CompanyManage.vue'),
                meta: { title: '公司信息' }
            }
        ]
    },
    {
        path: '/seeker',
        component: () => import('@/layouts/SeekerLayout.vue'),
        meta: { requiresAuth: true, role: 'SEEKER' },
        children: [
            {
                path: '',
                redirect: '/seeker/resume'
            },
            {
                path: 'resume',
                name: 'MyResume',
                component: () => import('@/views/seeker/MyResume.vue'),
                meta: { title: '我的简历' }
            },
            {
                path: 'applications',
                name: 'MyApplications',
                component: () => import('@/views/seeker/MyApplications.vue'),
                meta: { title: '投递记录' }
            },
            {
                path: 'favorites',
                name: 'MyFavorites',
                component: () => import('@/views/seeker/MyFavorites.vue'),
                meta: { title: '我的收藏' }
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '注册' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - Boss Job` : 'Boss Job'

    const userStore = useUserStore()

    // 检查是否需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }

    // 检查角色权限
    if (to.meta.role && userStore.user?.role !== to.meta.role) {
        next({ name: 'Home' })
        return
    }

    next()
})

export default router
