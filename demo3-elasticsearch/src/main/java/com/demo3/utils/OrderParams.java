package com.demo3.utils;

import lombok.Data;

@Data
public class OrderParams {
    /**
     * 排序字段
     */
    private String orderField;
    /**
     * 排序类型 ASC、DESC
     */
    private String orderType;
}
