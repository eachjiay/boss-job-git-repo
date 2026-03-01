package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bossjob.entity.Job;
import com.bossjob.entity.Resume;
import com.bossjob.mapper.JobMapper;
import com.bossjob.mapper.ResumeMapper;
import com.bossjob.service.AiResumeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import okhttp3.*;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 简历解析服务实现
 * 使用豆包(Doubao) API 解析简历
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiResumeServiceImpl implements AiResumeService {

    private final MinioClient minioClient;
    private final ObjectMapper objectMapper;
    private final ResumeMapper resumeMapper;
    private final JobMapper jobMapper;

    @Value("${doubao.api-key:}")
    private String apiKey;

    @Value("${doubao.model:doubao-pro-32k}")
    private String model;

    @Value("${doubao.endpoint:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final String RESUME_PARSE_PROMPT = """
            # ROLE
            你是一个精准的简历数据提取引擎。你的任务是根据提供的 [SOURCE_TEXT] 提取真实信息。

            # 核心指令 (必须遵守)
            1. **禁止幻觉**：如果 [SOURCE_TEXT] 中不存在某个字段的信息，请务必将其设置为 `null`。
            2. **禁止使用示例数据**：绝对不要使用“张三”、“Micky”、“13800...”、"test@..."等系统预设的示例数据作为结果。
            3. **输出格式**：只输出一个干净的 JSON 对象。不要包含 Markdown 代码块（如 ```json）、开场白或任何解释。
            4. **语言要求**：姓名、学校、专业、工作内容等字段应保持原语言（通常为中文）。

            # 目标字段及定义
            - realName: 真实姓名（核心必填，找不到填 null）
            - gender: 性别（男/女）
            - age: 年龄（数字）
            - phone: 手机号码/联系电话
            - email: 电子邮箱
            - education: 学历（如：本科、硕士、博士等）
            - school: 毕业院校
            - major: 专业名
            - graduationYear: 毕业年份（数字）
            - workYears: 工作年限（如：3年、实习等）
            - expectCity: 期望城市
            - expectPosition: 期望职位
            - expectSalaryMin, expectSalaryMax: 期望薪资范围（数字，单位为元/月）
            - introduction: 个人总结/自我评价
            - skills: 技能关键词（字符串数组，如 ["Java", "Vue"]）
            - internshipExperience: 实习经历（保持 JSON 数组结构）
            - workExperience: 工作经历（保持 JSON 数组结构）
            - projectExperience: 项目经历（保持 JSON 数组结构）

            [SOURCE_TEXT]:
            """;

    private static final String CANDIDATE_ANALYSIS_PROMPT = """
            # ROLE
            你是一个资深的 HR 招聘顾问和技术专家。请根据提供的简历数据，为招聘方提供核心分析。

            # 任务
            1. **核心优势**：总结求职者的 3 个核心优势。
            2. **面试建议**：针对该求职者的背景，提出 3 个有深度的面试问题。

            # 输出格式 (严格遵守)
            仅输出一个 JSON 对象，不要包含 Markdown 标记。格式：
            {
              "highlights": ["优势1", "优势2", "优势3"],
              "interviewQuestions": ["问题1", "问题2", "问题3"]
            }

            [RESUME_DATA]:
            """;

    private static final String MATCH_SCORE_PROMPT = """
            # ROLE
            你是一个资深的招聘技术专家。请评估提供的 [JOB_DESCRIPTION] 和 [RESUME_DATA] 之间的匹配度。

            # 任务
            1. **分值**：给出 0-100 的匹配分值。
            2. **理由**：简洁阐述 2 点匹配的理由。

            # 输出格式
            仅输出一个 JSON 对象：
            {
              "score": 85,
              "reason": ["符合XX要求", "具备XX背景"]
            }

            [JOB_DESCRIPTION]:
            """;

    // 使用 OkHttp 并配置宽松的连接规格
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Optimized timeouts
            .readTimeout(120, TimeUnit.SECONDS) // Reduced back to 2m as we expect faster response now
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionSpecs(List.of(
                    new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                            .build(),
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.CLEARTEXT))
            .build();

    @Override
    public Resume parseResumeAttachment(String attachmentUrl) {
        try {
            String text = extractTextFromUrl(attachmentUrl);
            if (!StringUtils.hasText(text) || text.trim().length() < 10) {
                log.error("Resume extraction failed: Text is too short or empty. Content: [{}]", text);
                throw new RuntimeException("无法从简历文件中提取有效文本，请确认文件不是纯图片扫描件");
            }

            // LOUD LOGS for debugging
            System.out.println("DEBUG - EXTRACTED TEXT LENGTH: " + text.length());
            System.out.println("DEBUG - PREVIEW: " + (text.length() > 200 ? text.substring(0, 200) : text));

            log.info("--- START EXTRACTED TEXT (Len:{}) ---", text.length());
            log.info("\n{}\n", text.length() > 5000 ? text.substring(0, 5000) : text);
            log.info("--- END EXTRACTED TEXT ---");

            return parseResumeText(text);
        } catch (Exception e) {
            log.error("Failed to parse resume attachment: {}", e.getMessage(), e);
            throw new RuntimeException("简历解析失败: " + e.getMessage());
        }
    }

    @Override
    public Resume parseResumeText(String resumeText) {
        if (!StringUtils.hasText(apiKey)) {
            throw new RuntimeException("请先配置豆包API Key");
        }

        try {
            // 扩大处理范围：将截断长度增加到 5000 以处理更详细的简历
            String truncatedText = resumeText.length() > 5000 ? resumeText.substring(0, 5000) : resumeText;

            String requestBody = objectMapper.writeValueAsString(new DoubaoRequest(
                    model,
                    List.of(new DoubaoMessage("user", RESUME_PARSE_PROMPT + truncatedText))));

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            log.info("Calling Doubao API: {}", endpoint);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.error("Doubao API error: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("AI接口调用失败: " + response.code());
                }

                String responseBody = response.body().string();
                log.info("Doubao API response: {}", responseBody);

                JsonNode root = objectMapper.readTree(responseBody);
                if (!root.has("choices") || root.get("choices").isEmpty()) {
                    throw new RuntimeException("AI响应格式错误: 缺少 choices");
                }

                String content = root.path("choices").get(0).path("message").path("content").asText();

                // 智能提取JSON
                String jsonContent = extractValidJson(content);
                log.info("Extracted JSON content: {}", jsonContent); // Added debug log
                if (jsonContent == null) {
                    throw new RuntimeException("未能从AI响应中提取有效的JSON数据");
                }

                return parseJsonToResume(jsonContent);
            }

        } catch (Exception e) {
            log.error("Failed to call Doubao API: {}", e.getMessage(), e);
            throw new RuntimeException("AI解析失败: " + e.getMessage());
        }
    }

    @Override
    public String getCandidateAnalysis(Long userId) {
        Resume resume = resumeMapper.selectOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId));
        if (resume == null) {
            return "{\"error\": \"未找到简历数据\"}";
        }

        try {
            String resumeData = objectMapper.writeValueAsString(resume);

            String requestBody = objectMapper.writeValueAsString(new DoubaoRequest(
                    model,
                    List.of(new DoubaoMessage("user", CANDIDATE_ANALYSIS_PROMPT + resumeData))));

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());
                    String content = root.path("choices").get(0).path("message").path("content").asText();
                    return extractValidJson(content);
                }
            }
        } catch (Exception e) {
            log.error("AI Analysis failed", e);
        }
        return "{\"error\": \"AI 分析暂时不可用\"}";
    }

    @Override
    public String calculateMatchScore(Long jobId, Long resumeId) {
        Job job = jobMapper.selectById(jobId);
        Resume resume = resumeMapper.selectById(resumeId);
        if (job == null || resume == null) {
            return "{\"score\": 0, \"reason\": [\"数据缺失\"]}";
        }

        try {
            String combinedData = String.format("\n[JOB]: %s\n[RESUME]: %s",
                    objectMapper.writeValueAsString(job),
                    objectMapper.writeValueAsString(resume));

            String requestBody = objectMapper.writeValueAsString(new DoubaoRequest(
                    model,
                    List.of(new DoubaoMessage("user", MATCH_SCORE_PROMPT + combinedData))));

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());
                    String content = root.path("choices").get(0).path("message").path("content").asText();
                    return extractValidJson(content);
                }
            }
        } catch (Exception e) {
            log.error("AI Match failed", e);
        }
        return "{\"score\": 50, \"reason\": [\"AI计算超时\"]}";
    }

    /**
     * 从混合文本中提取有效的JSON字符串
     * 解决AI返回 Chain-of-Thought 或 Markdown 包裹的问题
     */
    private String extractValidJson(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }

        // 1. 尝试直接解析
        if (isValidResumeJson(content)) {
            return content;
        }

        // 2. 寻找最大的 JSON 对象 { ... }
        // 从第一个 { 开始找，一直找到最后一个 }
        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");

        if (startIndex != -1 && endIndex > startIndex) {
            String candidate = content.substring(startIndex, endIndex + 1);
            if (isValidResumeJson(candidate)) {
                return candidate;
            }
        }

        return null; // 无法提取有效JSON
    }

    private boolean isValidResumeJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            // 简单校验是否包含关键字段，避免解析到无关的JSON（比如 error response）
            return node.has("realName") || node.has("education") || node.has("phone");
        } catch (Exception e) {
            return false;
        }
    }

    private String extractTextFromUrl(String url) throws Exception {
        // 判断是MinIO内部URL还是外部URL
        if (url.contains("localhost") || url.contains("127.0.0.1") || url.contains(bucketName)) {
            return extractFromMinio(url);
        } else {
            return extractFromExternalUrl(url);
        }
    }

    private String extractFromMinio(String url) throws Exception {
        // 从URL中提取对象名
        String objectName = url.substring(url.lastIndexOf("/") + 1);

        try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build())) {

            if (objectName.toLowerCase().endsWith(".pdf")) {
                return extractTextFromPdf(is);
            } else if (objectName.toLowerCase().endsWith(".docx")) {
                return extractTextFromDocx(is);
            } else {
                throw new RuntimeException("不支持的文件格式，请上传PDF或Word文档");
            }
        }
    }

    private String extractFromExternalUrl(String url) throws Exception {
        try (InputStream is = new URL(url).openStream()) {
            if (url.toLowerCase().endsWith(".pdf")) {
                return extractTextFromPdf(is);
            } else if (url.toLowerCase().endsWith(".docx")) {
                return extractTextFromDocx(is);
            } else {
                throw new RuntimeException("不支持的文件格式，请上传PDF或Word文档");
            }
        }
    }

    private String extractTextFromPdf(InputStream is) throws Exception {
        try (PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream is) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                sb.append(para.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    private Resume parseJsonToResume(String jsonContent) throws Exception {
        JsonNode node = objectMapper.readTree(jsonContent);

        Resume resume = new Resume();
        resume.setRealName(getTextOrNull(node, "realName"));
        resume.setGender(getTextOrNull(node, "gender"));
        resume.setAge(getIntOrNull(node, "age"));
        resume.setPhone(getTextOrNull(node, "phone"));
        resume.setEmail(getTextOrNull(node, "email"));
        resume.setEducation(getTextOrNull(node, "education"));
        resume.setSchool(getTextOrNull(node, "school"));
        resume.setMajor(getTextOrNull(node, "major"));
        resume.setGraduationYear(getIntOrNull(node, "graduationYear"));
        resume.setWorkYears(getTextOrNull(node, "workYears"));
        resume.setExpectCity(getTextOrNull(node, "expectCity"));
        resume.setExpectPosition(getTextOrNull(node, "expectPosition"));
        resume.setExpectSalaryMin(getIntOrNull(node, "expectSalaryMin"));
        resume.setExpectSalaryMax(getIntOrNull(node, "expectSalaryMax"));
        resume.setIntroduction(getTextOrNull(node, "introduction"));

        // 技能数组
        JsonNode skillsNode = node.get("skills");
        if (skillsNode != null && skillsNode.isArray()) {
            List<String> skills = new ArrayList<>();
            for (JsonNode skill : skillsNode) {
                skills.add(skill.asText());
            }
            resume.setSkills(objectMapper.writeValueAsString(skills));
        }

        // 实习经历
        if (node.has("internshipExperience")) {
            resume.setInternshipExperience(node.get("internshipExperience").toString());
        }

        // 工作经历
        if (node.has("workExperience")) {
            resume.setWorkExperience(node.get("workExperience").toString());
        }

        // 项目经历
        if (node.has("projectExperience")) {
            resume.setProjectExperience(node.get("projectExperience").toString());
        }

        return resume;
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull() || "null".equals(fieldNode.asText())) {
            return null;
        }
        return fieldNode.asText();
    }

    private Integer getIntOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull() || !fieldNode.isNumber()) {
            return null;
        }
        return fieldNode.asInt();
    }

    // 内部类用于构建请求
    record DoubaoRequest(String model, List<DoubaoMessage> messages) {
    }

    record DoubaoMessage(String role, String content) {
    }
}
