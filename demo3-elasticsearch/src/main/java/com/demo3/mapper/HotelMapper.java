package com.demo3.mapper;

import com.demo3.entity.Hotel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author redyo
* @description 针对表【tb_hotel】的数据库操作Mapper
* @createDate 2023-11-22 11:31:36
* @Entity demo3.entity.Hotel
*/
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {

}




