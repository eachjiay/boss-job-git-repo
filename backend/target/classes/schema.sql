-- H2 Database Schema for Boss Job Platform

-- 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    wechat_open_id VARCHAR(255) UNIQUE,
    real_name VARCHAR(50),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'SEEKER',
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    logo VARCHAR(500),
    scale VARCHAR(50),
    industry VARCHAR(100),
    financing VARCHAR(50),
    description TEXT,
    detail TEXT,
    benefits TEXT,
    address VARCHAR(255),
    city VARCHAR(50),
    images TEXT,
    business_info TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 职位表
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    job_type VARCHAR(50),
    city VARCHAR(50),
    address VARCHAR(255),
    salary_min INT,
    salary_max INT,
    experience VARCHAR(50),
    education VARCHAR(50),
    description TEXT,
    responsibility TEXT,
    requirement TEXT,
    benefits TEXT,
    keywords TEXT,
    status INT NOT NULL DEFAULT 1,
    view_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 简历表
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    real_name VARCHAR(50),
    gender VARCHAR(10),
    age INT,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500),
    education VARCHAR(50),
    school VARCHAR(100),
    major VARCHAR(100),
    graduation_year INT,
    work_years VARCHAR(50),
    job_status VARCHAR(50),
    expect_city VARCHAR(100),
    expect_position VARCHAR(100),
    expect_salary_min INT,
    expect_salary_max INT,
    introduction TEXT,
    skills TEXT,
    work_experience TEXT,
    project_experience TEXT,
    education_experience TEXT,
    attachment_url VARCHAR(500),
    internship_experience TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 投递记录表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    resume_id BIGINT,
    company_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    attachment_url VARCHAR(500),
    message_type INT DEFAULT 1,         -- 1=文本, 2=简历申请, 3=系统提示
    is_read INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 会话表 (用于优化会话列表查询)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_a BIGINT NOT NULL,           -- 用户A (ID较小)
    user_id_b BIGINT NOT NULL,           -- 用户B (ID较大)
    last_message_id BIGINT,
    last_message_content VARCHAR(500),
    last_message_time TIMESTAMP,
    unread_count_a INT DEFAULT 0,        -- A对B的未读数
    unread_count_b INT DEFAULT 0,        -- B对A的未读数
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users (user_id_a, user_id_b)
);

-- 职位收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,              -- 收藏用户
    job_id BIGINT NOT NULL,               -- 收藏的职位
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_job (user_id, job_id)
);
