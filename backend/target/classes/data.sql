-- 初始测试数据

-- 测试用户 (密码: 123456, BCrypt加密后)
INSERT INTO users (username, password, email, phone, real_name, role) VALUES
('seeker1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seeker1@test.com', '13800138001', '张三', 'SEEKER'),
('seeker2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'seeker2@test.com', '13800138002', '李四', 'SEEKER'),
('hr1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'hr1@test.com', '13800138003', '王HR', 'RECRUITER'),
('hr2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'hr2@test.com', '13800138004', '赵HR', 'RECRUITER');

-- 测试公司
INSERT INTO companies (user_id, name, logo, scale, industry, financing, description, city, benefits) VALUES
(3, '字节跳动', NULL, '10000人以上', '互联网', '已上市', '字节跳动是全球领先的移动互联网公司', '北京', '["五险一金", "带薪年假", "弹性工作", "免费三餐"]'),
(4, '阿里巴巴', NULL, '10000人以上', '互联网/电商', '已上市', '阿里巴巴集团是中国最大的电子商务公司', '杭州', '["五险一金", "股票期权", "年终奖金", "节日福利"]');

-- 测试职位
INSERT INTO jobs (company_id, publisher_id, title, job_type, city, salary_min, salary_max, experience, education, description, responsibility, requirement, keywords, benefits, status, view_count) VALUES
(1, 3, 'Java开发工程师', '社招全职', '北京', 25, 50, '3-5年', '本科', '负责公司核心业务系统的开发和维护', '1. 负责后端服务开发\n2. 参与系统架构设计\n3. 编写技术文档', '1. 本科及以上学历\n2. 3年以上Java开发经验\n3. 熟悉Spring Boot、MyBatis等框架', '["Java", "Spring Boot", "MySQL", "Redis"]', '["五险一金", "弹性工作", "免费三餐"]', 1, 1520),
(1, 3, '前端开发工程师', '社招全职', '北京', 20, 40, '1-3年', '本科', '负责公司Web前端开发', '1. 负责前端页面开发\n2. 优化页面性能\n3. 与后端协作对接API', '1. 本科及以上学历\n2. 熟悉Vue/React\n3. 了解Node.js', '["Vue", "React", "TypeScript", "Node.js"]', '["五险一金", "弹性工作", "免费三餐"]', 1, 890),
(2, 4, 'Python开发工程师', '社招全职', '杭州', 30, 60, '3-5年', '本科', '负责数据平台开发', '1. 负责数据处理系统开发\n2. 优化数据处理性能', '1. 熟悉Python\n2. 了解大数据技术', '["Python", "Spark", "Hadoop", "Flink"]', '["五险一金", "股票期权", "年终奖"]', 1, 650),
(2, 4, '产品经理', '社招全职', '杭州', 25, 45, '3-5年', '本科', '负责电商产品规划', '1. 负责产品需求分析\n2. 撰写PRD文档\n3. 跟进产品上线', '1. 3年以上产品经验\n2. 有电商经验优先', '["产品规划", "需求分析", "电商"]', '["五险一金", "股票期权"]', 1, 420);

-- 测试简历
INSERT INTO resumes (user_id, real_name, gender, age, phone, email, education, school, major, graduation_year, work_years, job_status, expect_city, expect_position, expect_salary_min, expect_salary_max, introduction, skills) VALUES
(1, '张三', '男', 26, '13800138001', 'seeker1@test.com', '本科', '清华大学', '计算机科学', 2022, '1-3年', '在职-考虑机会', '北京', 'Java开发工程师', 25, 40, '热爱编程，有较强的学习能力', '["Java", "Spring Boot", "MySQL", "Redis", "Docker"]'),
(2, '李四', '女', 24, '13800138002', 'seeker2@test.com', '硕士', '北京大学', '软件工程', 2024, '在校/应届', '离职-随时到岗', '深圳', '前端开发工程师', 15, 25, '应届研究生，实习经验丰富', '["Vue", "React", "TypeScript", "Node.js"]');

-- 测试投递记录 (seeker1 投递 Java)
INSERT INTO applications (job_id, user_id, resume_id, company_id, status) VALUES
(1, 1, 1, 1, 'CHATTING'),
(2, 2, 2, 1, 'PENDING');

-- 测试消息记录 (seeker1 和 hr1 的对话)
INSERT INTO messages (sender_id, receiver_id, content, is_read, create_time) VALUES
(1, 3, '您好，我对贵公司的Java开发职位很感兴趣，请问还可以投递吗？', 1, DATEADD('MINUTE', -60, CURRENT_TIMESTAMP)),
(3, 1, '你好，可以的，请发送简历。', 1, DATEADD('MINUTE', -55, CURRENT_TIMESTAMP)),
(1, 3, '好的，简历已发送，请查收。', 1, DATEADD('MINUTE', -50, CURRENT_TIMESTAMP)),
(3, 1, '收到，我们评估后会联系你。', 0, DATEADD('MINUTE', -5, CURRENT_TIMESTAMP));

-- 测试消息记录 (seeker1 和 hr2 的对话 - 只有一条)
INSERT INTO messages (sender_id, receiver_id, content, is_read, create_time) VALUES
(4, 1, '你好，看到你的简历很匹配我们公司的岗位，有兴趣聊聊吗？', 0, DATEADD('MINUTE', -120, CURRENT_TIMESTAMP));
