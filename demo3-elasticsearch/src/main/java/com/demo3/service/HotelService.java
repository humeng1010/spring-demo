package com.demo3.service;

import com.demo3.entity.Hotel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.demo3.model.dto.HotelDto;
import com.demo3.model.req.QueryHotelByPageReq;

import java.util.List;

/**
* @author redyo
* @description 针对表【tb_hotel】的数据库操作Service
* @createDate 2023-11-22 11:31:36
*/
public interface HotelService extends IService<Hotel> {

    List<HotelDto> searchHotelByES(QueryHotelByPageReq queryHotelByPageReq);
}
