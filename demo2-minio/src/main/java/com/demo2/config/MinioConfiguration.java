package com.demo2.config;

import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MinioClient.class})
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnExpression("${oss.enabled}")
@ConditionalOnProperty(value = "oss.type", havingValue = "minio")
public class MinioConfiguration {


//    @Bean
//    @SneakyThrows
//    @ConditionalOnMissingBean(MinioClient.class)
//    public MinioClient minioClient(OssProperties ossProperties) {
//        return MinioClient.builder()
//                .endpoint(ossProperties.getEndpoint())
//                .credentials(ossProperties.getAccessKey(), ossProperties.getSecretKey())
//                .build();
//    }

    @Bean
    @ConditionalOnBean({MinioClient.class})
    @ConditionalOnMissingBean(MinioTemplate.class)
    public MinioTemplate minioTemplate(PearlMinioClient minioClient, OssProperties ossProperties) {
        return new MinioTemplate(minioClient, ossProperties);
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(PearlMinioClient.class)
    public PearlMinioClient minioClient(OssProperties ossProperties) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(ossProperties.getEndpoint())
                .credentials(ossProperties.getAccessKey(), ossProperties.getSecretKey())
                .build();
        return new PearlMinioClient(minioClient);
    }
}