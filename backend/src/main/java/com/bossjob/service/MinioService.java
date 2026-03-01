package com.bossjob.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    /**
     * 上传文件
     * 
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);
}
