package com.demo2.controller;

import com.demo2.config.MinioTemplate;
import io.minio.CreateMultipartUploadResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @Resource
    private MinioTemplate minioTemplate;

    @GetMapping("/upload")
    public Object upload(MultipartFile file, String bucketName) throws IOException {
        return minioTemplate.putObject(file.getInputStream(), bucketName, file.getOriginalFilename());
    }

    @GetMapping("/download")
    public String download(String bucketName, String ossFilePath){
        return minioTemplate.getPresignedObjectUrl(bucketName,ossFilePath,null);
    }

    /**
     * 返回分片上传需要的签名数据URL及 uploadId
     *
     * @param bucketName
     * @param fileName
     * @return
     */
    @GetMapping("/createMultipartUpload")
    @SneakyThrows
    @ResponseBody
    public Map<String, Object> createMultipartUpload(String bucketName, String fileName, Integer chunkSize) {
        // 1. 根据文件名创建签名
        Map<String, Object> result = new HashMap<>();
        // 2. 获取uploadId
        CreateMultipartUploadResponse response = minioTemplate.uploadId(bucketName, null, fileName, null, null);
        String uploadId = response.result().uploadId();
        result.put("uploadId", uploadId);
        // 3. 请求Minio 服务，获取每个分块带签名的上传URL
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("uploadId", uploadId);
        List<String> partList = new ArrayList<>();
        // 4. 循环分块数 从1开始
        for (int i = 1; i <= chunkSize; i++) {
            reqParams.put("partNumber", String.valueOf(i));
            String uploadUrl = minioTemplate.getPresignedObjectUrl(bucketName, fileName, reqParams);// 获取URL
            result.put("chunk_" + (i - 1), uploadUrl); // 添加到集合
        }
        return result;
    }
}
