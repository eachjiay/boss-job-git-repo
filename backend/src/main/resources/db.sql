-- Boss Job Platform 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS boss_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE boss_job;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'SEEKER' COMMENT '角色: SEEKER, RECRUITER, ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联的招聘者用户ID',
    name VARCHAR(100) NOT NULL COMMENT '公司名称',
    logo VARCHAR(500) COMMENT '公司Logo',
    scale VARCHAR(50) COMMENT '公司规模',
    industry VARCHAR(100) COMMENT '行业',
    financing VARCHAR(50) COMMENT '融资阶段',
    description TEXT COMMENT '公司简介',
    detail TEXT COMMENT '公司详细介绍',
    benefits TEXT COMMENT '公司福利, JSON数组',
    address VARCHAR(255) COMMENT '公司地址',
    city VARCHAR(50) COMMENT '城市',
    images TEXT COMMENT '公司图片, JSON数组',
    business_info TEXT COMMENT '工商信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司表';

-- 职位表
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL COMMENT '公司ID',
    publisher_id BIGINT NOT NULL COMMENT '发布者用户ID',
    title VARCHAR(100) NOT NULL COMMENT '职位名称',
    job_type VARCHAR(50) COMMENT '职位类型',
    city VARCHAR(50) COMMENT '工作城市',
    address VARCHAR(255) COMMENT '工作地点',
    salary_min INT COMMENT '最低薪资(K)',
    salary_max INT COMMENT '最高薪资(K)',
    experience VARCHAR(50) COMMENT '经验要求',
    education VARCHAR(50) COMMENT '学历要求',
    description TEXT COMMENT '职位描述',
    responsibility TEXT COMMENT '岗位职责',
    requirement TEXT COMMENT '任职要求',
    benefits TEXT COMMENT '职位福利, JSON数组',
    keywords TEXT COMMENT '职位关键词, JSON数组',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已下线, 1-招聘中',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company_id (company_id),
    INDEX idx_city (city),
    INDEX idx_status (status),
    INDEX idx_salary (salary_min, salary_max),
    FULLTEXT INDEX ft_title_desc (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 简历表
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    education VARCHAR(50) COMMENT '最高学历',
    school VARCHAR(100) COMMENT '毕业院校',
    major VARCHAR(100) COMMENT '专业',
    graduation_year INT COMMENT '毕业年份',
    work_years VARCHAR(50) COMMENT '工作年限',
    job_status VARCHAR(50) COMMENT '求职状态',
    expect_city VARCHAR(100) COMMENT '期望城市',
    expect_position VARCHAR(100) COMMENT '期望职位',
    expect_salary_min INT COMMENT '期望最低薪资(K)',
    expect_salary_max INT COMMENT '期望最高薪资(K)',
    introduction TEXT COMMENT '个人介绍',
    skills TEXT COMMENT '技能标签, JSON数组',
    work_experience TEXT COMMENT '工作经历, JSON数组',
    project_experience TEXT COMMENT '项目经历, JSON数组',
    education_experience TEXT COMMENT '教育经历, JSON数组',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_expect_city (expect_city),
    INDEX idx_education (education)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- 投递记录表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL COMMENT '职位ID',
    user_id BIGINT NOT NULL COMMENT '申请者用户ID',
    resume_id BIGINT COMMENT '简历ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, VIEWED, CHATTING, INTERVIEW, OFFER, HIRED, REJECTED',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_job_user (job_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录表';

-- 插入测试数据

-- 测试用户 (密码均为: 123456)
INSERT INTO users (username, password, email, phone, real_name, role) VALUES
('seeker1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seeker1@test.com', '13800138001', '张三', 'SEEKER'),
('seeker2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seeker2@test.com', '13800138002', '李四', 'SEEKER'),
('hr1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'hr1@test.com', '13800138003', '王HR', 'RECRUITER'),
('hr2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'hr2@test.com', '13800138004', '赵HR', 'RECRUITER');

-- 测试公司
INSERT INTO companies (user_id, name, logo, scale, industry, financing, description, city, benefits) VALUES
(3, '字节跳动', 'https://img.bosszhipin.com/beijin/icon/894dc9db61.png', '10000人以上', '互联网', '已上市', '字节跳动是全球领先的移动互联网公司', '北京', '["五险一金", "带薪年假", "弹性工作", "免费三餐"]'),
(4, '阿里巴巴', 'https://img.bosszhipin.com/beijin/icon/894dc9db62.png', '10000人以上', '互联网/电商', '已上市', '阿里巴巴集团是中国最大的电子商务公司', '杭州', '["五险一金", "股票期权", "年终奖金", "节日福利"]');

-- 测试职位
INSERT INTO jobs (company_id, publisher_id, title, job_type, city, salary_min, salary_max, experience, education, description, responsibility, requirement, keywords, benefits, status, view_count) VALUES
(1, 3, 'Java开发工程师', '社招全职', '北京', 25, 50, '3-5年', '本科', 
 '负责公司核心业务系统的开发和维护', 
 '1. 负责后端服务开发\n2. 参与系统架构设计\n3. 编写技术文档',
 '1. 本科及以上学历\n2. 3年以上Java开发经验\n3. 熟悉Spring Boot、MyBatis等框架',
 '["Java", "Spring Boot", "MySQL", "Redis"]',
 '["五险一金", "弹性工作", "免费三餐"]', 1, 1520),
 
(1, 3, '前端开发工程师', '社招全职', '北京', 20, 40, '1-3年', '本科',
 '负责公司Web前端开发',
 '1. 负责前端页面开发\n2. 优化页面性能\n3. 与后端协作对接API',
 '1. 本科及以上学历\n2. 熟悉Vue/React\n3. 了解Node.js',
 '["Vue", "React", "TypeScript", "Node.js"]',
 '["五险一金", "弹性工作", "免费三餐"]', 1, 890),

(2, 4, 'Python开发工程师', '社招全职', '杭州', 30, 60, '3-5年', '本科',
 '负责数据平台开发',
 '1. 负责数据处理系统开发\n2. 优化数据处理性能',
 '1. 熟悉Python\n2. 了解大数据技术',
 '["Python", "Spark", "Hadoop", "Flink"]',
 '["五险一金", "股票期权", "年终奖"]', 1, 650),

(2, 4, '产品经理', '社招全职', '杭州', 25, 45, '3-5年', '本科',
 '负责电商产品规划',
 '1. 负责产品需求分析\n2. 撰写PRD文档\n3. 跟进产品上线',
 '1. 3年以上产品经验\n2. 有电商经验优先',
 '["产品规划", "需求分析", "电商"]',
 '["五险一金", "股票期权"]', 1, 420);

-- 测试简历
INSERT INTO resumes (user_id, real_name, gender, age, phone, email, education, school, major, graduation_year, work_years, job_status, expect_city, expect_position, expect_salary_min, expect_salary_max, introduction, skills) VALUES
(1, '张三', '男', 26, '13800138001', 'seeker1@test.com', '本科', '清华大学', '计算机科学', 2022, '1-3年', '在职-考虑机会', '北京', 'Java开发工程师', 25, 40, '热爱编程，有较强的学习能力', '["Java", "Spring Boot", "MySQL", "Redis", "Docker"]'),
(2, '李四', '女', 24, '13800138002', 'seeker2@test.com', '硕士', '北京大学', '软件工程', 2024, '在校/应届', '离职-随时到岗', '深圳', '前端开发工程师', 15, 25, '应届研究生，实习经验丰富', '["Vue", "React", "TypeScript", "Node.js"]');

-- 测试投递记录
INSERT INTO applications (job_id, user_id, resume_id, company_id, status) VALUES
(1, 1, 1, 1, 'CHATTING'),
(2, 2, 2, 1, 'PENDING');
