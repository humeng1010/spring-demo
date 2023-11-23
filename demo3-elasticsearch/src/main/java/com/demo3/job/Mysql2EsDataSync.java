package com.demo3.job;

import com.demo3.entity.Hotel;
import com.demo3.model.docs.HotelDoc;
import com.demo3.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Mysql2EsDataSync {
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private HotelService hotelService;

    /**
     * FIXME 粒度不好控制，后续采用消息队列的方式在增删改的后面加上发送消息的代码
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncData(){
        // FIXME 分布式锁需要加上，多机部署的时候防止重复执行
        log.info("==============>mysql数据同步es数据定时任务执行");
        List<Hotel> hotelList = hotelService.list();
        // DO -> DOC
        List<HotelDoc> hotelDocList = hotelList.stream().map(HotelDoc::new).collect(Collectors.toList());
        elasticsearchRestTemplate.save(hotelDocList);
        log.info("<==============mysql数据同步es数据定时任务完成");
    }
}
