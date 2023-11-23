package com.demo3.utils;

import com.demo3.model.req.QueryHotelByPageReq;

import java.util.Optional;

public class PageParamsUtil {
    public static void adaptPage(QueryHotelByPageReq queryHotelByPageReq) {
        Optional.ofNullable(queryHotelByPageReq.getPageNo()).ifPresent(e->queryHotelByPageReq.setPageNo(1));
        Optional.ofNullable(queryHotelByPageReq.getPageSize()).ifPresent(e->queryHotelByPageReq.setPageSize(10));
    }
}
