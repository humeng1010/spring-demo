package com.demo3.mapper;

import com.demo3.model.docs.HotelDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelEsMapper extends ElasticsearchRepository<HotelDoc,Long> {
}
