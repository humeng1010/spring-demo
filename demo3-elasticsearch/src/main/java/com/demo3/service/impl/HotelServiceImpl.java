package com.demo3.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo3.constants.HotelDocField;
import com.demo3.entity.Hotel;
import com.demo3.model.docs.HotelDoc;
import com.demo3.model.dto.HotelDto;
import com.demo3.model.req.QueryHotelByPageReq;
import com.demo3.service.HotelService;
import com.demo3.mapper.HotelMapper;
import com.demo3.utils.OrderParamsUtil;
import com.demo3.utils.PageParamsUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author redyo
* @description 针对表【tb_hotel】的数据库操作Service实现
* @createDate 2023-11-22 11:31:36
*/
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel>
    implements HotelService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Override
    public List<HotelDto> searchHotelByES(QueryHotelByPageReq queryHotelByPageReq) {
        PageParamsUtil.adaptPage(queryHotelByPageReq);
        OrderParamsUtil.adaptOrdered(queryHotelByPageReq);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = searchQueryBuild(queryHotelByPageReq);
        nativeSearchQueryBuilder
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(queryHotelByPageReq.getPageNo(),queryHotelByPageReq.getPageSize()));
        SearchHits<HotelDoc> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), HotelDoc.class);
        List<SearchHit<HotelDoc>> hits = searchHits.getSearchHits();

        List<HotelDto> hotelDtoList = new ArrayList<>(hits.size());
        for (SearchHit<HotelDoc> hit : hits) {
            HotelDoc content = hit.getContent();
            HotelDto hotelDto = new HotelDto();
            BeanUtils.copyProperties(content, hotelDto);
            String[] latAndLong = content.getLocation().split(",");
            hotelDto.setLatitude(latAndLong[0]);
            hotelDto.setLongitude(latAndLong[1].substring(1));
            hotelDtoList.add(hotelDto);
        }
        return hotelDtoList;
    }

    private BoolQueryBuilder searchQueryBuild(QueryHotelByPageReq queryHotelByPageReq) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (queryHotelByPageReq.getId()!=null){
            queryBuilder.must(QueryBuilders.termQuery(HotelDocField.ID,queryHotelByPageReq.getId()));
        }
        if (!StringUtils.isEmpty(queryHotelByPageReq.getName())){
            queryBuilder.must(QueryBuilders.matchQuery(HotelDocField.NAME,queryHotelByPageReq.getName()));
        }
        if (!StringUtils.isEmpty(queryHotelByPageReq.getKeyword())){
            queryBuilder.must(QueryBuilders.matchQuery(HotelDocField.ALL,queryHotelByPageReq.getKeyword()));
        }

        return queryBuilder;
    }
}




