package com.demo3.utils;

import com.demo3.constants.HotelDocField;
import com.demo3.model.req.QueryHotelByPageReq;
import org.springframework.util.StringUtils;

public class OrderParamsUtil {
    public static void adaptOrdered(QueryHotelByPageReq queryHotelByPageReq) {
        if (StringUtils.isEmpty(queryHotelByPageReq.getOrderField())){
            queryHotelByPageReq.setOrderField(HotelDocField.SCORE);
        }
        if (StringUtils.isEmpty(queryHotelByPageReq.getOrderType())){
            queryHotelByPageReq.setOrderType("DESC");
        }
    }
}
