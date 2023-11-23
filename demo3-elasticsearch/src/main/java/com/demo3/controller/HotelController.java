package com.demo3.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo3.model.dto.HotelDto;
import com.demo3.model.req.QueryHotelByPageReq;
import com.demo3.model.vo.HotelVO;
import com.demo3.service.HotelService;
import com.demo3.utils.Res;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private HotelService hotelService;

    @PostMapping("/search")
    public Res<IPage<HotelVO>> searchHotelByPage(@RequestBody(required = false) QueryHotelByPageReq queryHotelByPageReq){
        if (queryHotelByPageReq == null){
            queryHotelByPageReq = new QueryHotelByPageReq();
        }
        List<HotelDto> hotelDtoList = hotelService.searchHotelByES(queryHotelByPageReq);

        List<HotelVO> hotelVOList = new ArrayList<>();
        for (HotelDto hotelDto : hotelDtoList) {
            HotelVO hotelVO = new HotelVO();
            BeanUtils.copyProperties(hotelDto, hotelVO);
            hotelVOList.add(hotelVO);
        }
        IPage<HotelVO> hotelVOPage = new Page<>(queryHotelByPageReq.getPageNo(),queryHotelByPageReq.getPageSize());
        hotelVOPage.setRecords(hotelVOList);

        return Res.createSuccess(hotelVOPage);
    }
}
