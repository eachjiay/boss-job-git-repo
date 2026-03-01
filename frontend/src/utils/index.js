// 格式化薪资
export function formatSalary(min, max) {
    if (!min && !max) return '面议'
    if (min === max) return `${min}K`
    return `${min}-${max}K`
}

// 格式化时间
export function formatTime(dateStr) {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date

    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 30) return `${days}天前`

    return date.toLocaleDateString('zh-CN')
}

// 申请状态映射
export const applicationStatusMap = {
    PENDING: { text: '待处理', type: 'info' },
    VIEWED: { text: '已查看', type: 'primary' },
    CHATTING: { text: '沟通中', type: 'warning' },
    INTERVIEW: { text: '已约面', type: 'success' },
    OFFER: { text: '已发offer', type: 'success' },
    HIRED: { text: '已入职', type: 'success' },
    REJECTED: { text: '不合适', type: 'danger' }
}

// 城市列表
export const cityList = [
    '北京', '上海', '深圳', '广州', '杭州', '成都', '南京', '武汉', '西安', '苏州',
    '天津', '重庆', '长沙', '郑州', '青岛', '大连', '宁波', '厦门', '福州', '合肥'
]

// 学历列表
export const educationList = ['不限', '大专', '本科', '硕士', '博士']

// 经验列表
export const experienceList = ['不限', '在校/应届', '1年以内', '1-3年', '3-5年', '5-10年', '10年以上']

// 薪资范围
export const salaryRanges = [
    { label: '不限', min: null, max: null },
    { label: '3K以下', min: 0, max: 3 },
    { label: '3-5K', min: 3, max: 5 },
    { label: '5-10K', min: 5, max: 10 },
    { label: '10-15K', min: 10, max: 15 },
    { label: '15-25K', min: 15, max: 25 },
    { label: '25-50K', min: 25, max: 50 },
    { label: '50K以上', min: 50, max: null }
]

// 公司规模
export const scaleList = ['0-20人', '20-99人', '100-499人', '500-999人', '1000-9999人', '10000人以上']

// 融资阶段
export const financingList = ['未融资', '天使轮', 'A轮', 'B轮', 'C轮', 'D轮及以上', '已上市', '不需要融资']
