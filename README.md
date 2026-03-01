# Boss Job Platform - 招聘平台

类似于Boss直聘的招聘平台，支持求职者和招聘者两种角色。

## 项目结构

```
boss-job-platform/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/bossjob/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # REST API 控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── exception/         # 异常处理
│   │   ├── mapper/            # MyBatis Mapper
│   │   ├── security/          # JWT 安全配置
│   │   ├── service/           # 业务逻辑层
│   │   └── vo/                # 视图对象
│   └── src/main/resources/
│       ├── application.yml    # 配置文件
│       └── db.sql            # 数据库初始化脚本
│
└── frontend/                  # Vue 3 前端
    ├── src/
    │   ├── layouts/           # 布局组件
    │   ├── router/            # 路由配置
    │   ├── stores/            # Pinia 状态管理
    │   ├── styles/            # 全局样式
    │   ├── utils/             # 工具函数
    │   └── views/             # 页面组件
    │       ├── recruiter/     # 招聘者页面
    │       └── seeker/        # 求职者页面
    └── package.json
```

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2
- MyBatis-Plus
- MySQL
- JWT 认证

### 前端
- Vue 3 (Composition API)
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios

## 快速开始

### 1. 数据库配置

1. 创建 MySQL 数据库
2. 执行 `backend/src/main/resources/db.sql` 初始化表结构和测试数据

### 2. 启动后端

```bash
cd backend
# 使用 IDE (IntelliJ IDEA / Eclipse) 打开项目
# 或者安装 Maven 后运行:
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 求职者 | seeker1 | 123456 |
| 求职者 | seeker2 | 123456 |
| 招聘者 | hr1 | 123456 |
| 招聘者 | hr2 | 123456 |

## 功能模块

### 求职者功能
- ✅ 首页职位推荐
- ✅ 职位搜索 (关键词、城市、学历、经验、薪资筛选)
- ✅ 职位详情查看
- ✅ 公司详情查看
- ✅ 在线投递简历
- ✅ 投递记录查看
- ✅ 个人简历管理

### 招聘者功能
- ✅ 人才库搜索
- ✅ 收到的简历管理 (状态流转)
- ✅ 职位发布
- ✅ 职位管理 (上线/下线)
- ✅ 公司信息管理

## API 接口

### 认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户

### 职位
- `GET /api/jobs/search` - 搜索职位
- `GET /api/jobs/hot` - 热门职位
- `GET /api/jobs/{id}` - 职位详情
- `POST /api/jobs` - 发布职位
- `PUT /api/jobs/{id}` - 更新职位
- `DELETE /api/jobs/{id}` - 下线职位

### 公司
- `GET /api/companies/{id}` - 公司详情
- `POST /api/companies` - 创建公司
- `PUT /api/companies/{id}` - 更新公司

### 简历
- `GET /api/resumes/my` - 我的简历
- `POST /api/resumes` - 保存简历
- `GET /api/resumes/search` - 搜索候选人

### 投递
- `POST /api/applications/apply/{jobId}` - 投递简历
- `GET /api/applications/my` - 我的投递
- `GET /api/applications/received` - 收到的投递
- `PUT /api/applications/{id}/status` - 更新状态

## 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/boss_job
    username: root
    password: root

jwt:
  secret: YourSecretKey
  expiration: 86400000  # 24小时
```

### 前端代理配置 (vite.config.js)

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 截图预览

项目界面参考 Boss 直聘风格设计，采用青色 (#00bebd) 作为主题色。

## License

MIT
