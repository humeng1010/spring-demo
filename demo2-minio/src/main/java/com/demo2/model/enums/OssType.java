package com.demo2.model.enums;


import lombok.Getter;

@Getter
public enum OssType {
    /**
     * Minio 对象存储
     */
    MINIO("minio", 1),

    /**
     * 华为 OBS
     */
    OBS("obs", 2),

    /**
     * 腾讯 COS
     */
    COS("tencent", 3),

    /**
     * 阿里巴巴 SSO
     */
    ALIBABA("alibaba", 4),
    ;

    /**
     * 名称
     */
    final String name;
    /**
     * 类型
     */
    final int type;

    OssType(String name, int type) {
        this.name = name;
        this.type = type;
    }


}
