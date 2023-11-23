package com.demo2.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.demo2.model.vo.OssFile;
import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Part;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class MinioTemplate {

    /**
     * MinIO 客户端
     */
    PearlMinioClient pearlMinioClient;

    /**
     * MinIO 配置类
     */
    OssProperties ossProperties;

    /**
     * 查询所有存储桶
     *
     * @return Bucket 集合
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return pearlMinioClient.listBuckets();
    }

    /**
     * 桶是否存在
     *
     * @param bucketName 桶名
     * @return 是否存在
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return pearlMinioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 桶名
     */
    @SneakyThrows
    public void makeBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            pearlMinioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

        }
    }

    /**
     * 删除一个空桶 如果存储桶存在对象不为空时，删除会报错。
     *
     * @param bucketName 桶名
     */
    @SneakyThrows
    public void removeBucket(String bucketName) {
        pearlMinioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 上传文件
     *
     * @param inputStream      流
     * @param originalFileName 原始文件名
     * @param bucketName       桶名
     * @return OssFile
     */
    @SneakyThrows
    public OssFile putObject(InputStream inputStream, String bucketName, String originalFileName) {
        String uuidFileName = generateOssUuidFileName(originalFileName);
        try {
            if (StrUtil.isEmpty(bucketName)) {
                bucketName = ossProperties.getDefaultBucketName();
            }
            pearlMinioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(uuidFileName).stream(
                                    inputStream, inputStream.available(), -1)
                            .build());
            return new OssFile(uuidFileName, originalFileName);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 返回临时带签名、过期时间一天、Get请求方式的访问URL
     *
     * @param bucketName  桶名
     * @param ossFilePath Oss文件路径
     * @return
     */
    @SneakyThrows
    public String getPresignedObjectUrl(String bucketName, String ossFilePath,Map<String, String> queryParams) {
        return pearlMinioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(ossFilePath)
                        .expiry(60 * 60 * 24)
                        .extraQueryParams(queryParams)
                        .build());
    }



    /**
     * GetObject接口用于获取某个文件（Object）。此操作需要对此Object具有读权限。
     *
     * @param bucketName  桶名
     * @param ossFilePath Oss文件路径
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String ossFilePath) {
        return pearlMinioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(ossFilePath).build());
    }

    /**
     * 查询桶的对象信息
     *
     * @param bucketName 桶名
     * @param recursive  是否递归查询
     * @return
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName, boolean recursive) {
        return pearlMinioClient.listObjects(
                ListObjectsArgs.builder().bucket("my-bucketname").recursive(recursive).build());
    }
    /**
     * 获取带签名的临时上传元数据对象，前端可获取后，直接上传到Minio
     *
     * @param bucketName
     * @param fileName
     * @return
     */
    @SneakyThrows
    public Map<String, String> getPresignedPostFormData(String bucketName, String fileName) {
        // 为存储桶创建一个上传策略，过期时间为7天
        PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusDays(1));
        // 设置一个参数key，值为上传对象的名称
        policy.addEqualsCondition("key", fileName);
        // 添加Content-Type，例如以"image/"开头，表示只能上传照片，这里吃吃所有
        policy.addStartsWithCondition("Content-Type", MediaType.ALL_VALUE);
        // 设置上传文件的大小 64kiB to 10MiB.
        //policy.addContentLengthRangeCondition(64 * 1024, 10 * 1024 * 1024);
        return pearlMinioClient.getPresignedPostFormData(policy);
    }

    /**
     *  上传分片上传请求，返回uploadId
     */
    public CreateMultipartUploadResponse uploadId(String bucketName, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return pearlMinioClient.createMultipartUpload(bucketName, region, objectName, headers, extraQueryParams);
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    public ObjectWriteResponse completeMultipartUpload(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return pearlMinioClient.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    public String generateOssUuidFileName(String originalFilename) {
        return "files" + StrUtil.SLASH + DateUtil.format(new Date(), "yyyy-MM-dd") + StrUtil.SLASH + UUID.randomUUID() + StrUtil.UNDERLINE + originalFilename;
    }

    /**
     * 初始化默认存储桶
     */
    @PostConstruct
    public void initDefaultBucket() {
        String defaultBucketName = ossProperties.getDefaultBucketName();
        if (bucketExists(defaultBucketName)) {
            log.info("默认存储桶：defaultBucketName已存在");
        } else {
            log.info("创建默认存储桶：defaultBucketName");
            makeBucket(ossProperties.getDefaultBucketName());
        }
        ;
    }

    /**
     * 查询分片数据
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    public ListPartsResponse listMultipart(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException, ServerException, InvalidKeyException {
        return pearlMinioClient.listMultipart(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }
}
