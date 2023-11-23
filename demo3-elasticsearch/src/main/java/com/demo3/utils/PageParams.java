package com.demo3.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PageParams extends OrderParams{
    private Integer pageNo;
    private Integer pageSize;
}
