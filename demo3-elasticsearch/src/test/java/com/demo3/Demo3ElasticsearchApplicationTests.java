package com.demo3;

import com.alibaba.fastjson.JSON;
import com.demo3.constants.HotelConstants;
import com.demo3.constants.HotelDocField;
import com.demo3.entity.Hotel;
import com.demo3.mapper.HotelEsMapper;
import com.demo3.model.docs.HotelDoc;
import com.demo3.service.HotelService;
import com.demo3.utils.Res;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;


@SpringBootTest
class Demo3ElasticsearchApplicationTests {

    @Resource
    private RestHighLevelClient client;

    @Test
    void testCreateIndex() throws IOException {
        // 1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.准备请求的参数：DSL语句
        request.source(HotelConstants.MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    void testDeleteIndex(){
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request,RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    void testGetIndex(){
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        Assertions.assertTrue(exists);
    }

    @Resource
    private HotelService hotelService;

    /**
     * 把数据库中的数据批量导入到es中
     * @throws IOException
     */
    @Test
    void testBulkRequest() throws IOException {
        // 批量查询酒店数据
        List<Hotel> hotels = hotelService.list();

        // 1.创建Request
        BulkRequest request = new BulkRequest();
        // 2.准备参数，添加多个新增的Request
        for (Hotel hotel : hotels) {
            // 2.1.转换为文档类型HotelDoc
            HotelDoc hotelDoc = new HotelDoc(hotel);
            // 2.2.创建新增文档的Request对象
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        // 3.发送请求
        client.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 查询文档
     *
     * @throws IOException
     */
    @Test
    void testGetDocumentById() throws IOException {
        // 1.准备Request
        GetRequest request = new GetRequest("hotel", "36934");
        // 2.发送请求，得到响应
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3.解析响应结果
        String json = response.getSourceAsString();

        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }


    /**
     * 删除文档
     * @throws IOException
     */
    @Test
    void testDelDocumentByIds() throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest("hotel").id("1"));
        request.add(new DeleteRequest("hotel").id("36934"));
        request.add(new DeleteRequest("hotel").id("38609"));
        client.bulk(request,RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDocument() throws IOException {
        //0.查询数据库中的数据
        List<Hotel> list = hotelService.list();
        // 1.创建Request
        BulkRequest request = new BulkRequest();
        for (Hotel hotel : list) {
            // 增量修改
            request.add(new UpdateRequest("hotel",hotel.getId().toString())
                    .doc(
                            "price", "952",
                            "starName", "四钻"
                    ));
        }
        //3.发送请求
        client.bulk(request,RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    void testMatchAll(){
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .query(QueryBuilders.matchAllQuery())
                .from(0).size(20);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        System.out.println(total);
        for (SearchHit hit : response.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println(hotelDoc);
        }
    }

    @Test
    @SneakyThrows
    void testMatch(){
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("name","外滩如家").analyzer("ik_max_word"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        System.out.println(total);
        for (SearchHit hit : response.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println(hotelDoc);
        }
    }

    @Test
    @SneakyThrows
    void testBoolQuery(){
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("city","上海"))
                        .must(QueryBuilders.rangeQuery("price").lte(150))
        );
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        System.out.println(total);
        for (SearchHit hit : response.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println(hotelDoc);
        }
    }

    @Test
    @SneakyThrows
    void testGEO(){
        SearchRequest request = new SearchRequest("hotel");
        request.source().sort(SortBuilders.geoDistanceSort("location",new GeoPoint(31.21,121.5))
                .order(SortOrder.ASC)
                .unit(DistanceUnit.KILOMETERS));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        System.out.println(total);
        for (SearchHit hit : response.getHits().getHits()) {
            for (Object sortValue : hit.getSortValues()) {
                System.out.println(sortValue);
            }
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println(hotelDoc);
        }
    }
    @Test
    void testHighlight() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        // 2.准备DSL
        // 2.1.query
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        // 2.2.高亮
        request.source().highlighter(new HighlightBuilder()
                .field("name")
                .requireFieldMatch(false)
                .preTags("<i>").postTags("</i>"));
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Text text = name.getFragments()[0];
            System.out.println(text);
        }

    }

    /**
     * 使用SpringBoot的elasticsearchRestTemplate来操作es
     */
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void testElasticSearchTemplate(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all","陆家嘴"))
                .withPageable(PageRequest.of(0,10))
                .withSort(SortBuilders.geoDistanceSort(HotelDocField.LOCATION,new GeoPoint(31.21,121.5))
                        .unit(DistanceUnit.KILOMETERS)
                        .order(SortOrder.ASC));

        SearchHits<HotelDoc> search = elasticsearchRestTemplate.search(queryBuilder.build(), HotelDoc.class);
        List<org.springframework.data.elasticsearch.core.SearchHit<HotelDoc>> searchHits = search.getSearchHits();
        for (org.springframework.data.elasticsearch.core.SearchHit<HotelDoc> searchHit : searchHits) {
            HotelDoc content = searchHit.getContent();
            List<Object> sortValues = searchHit.getSortValues();
            for (Object sortValue : sortValues) {
                System.out.println(sortValue+"km");
            }
            System.out.println(content);
        }

    }

    @Resource
    private HotelEsMapper hotelEsMapper;

    @Test
    void testEsMapper(){
        Iterable<HotelDoc> all = hotelEsMapper.findAll();
        for (HotelDoc hotelDoc : all) {
            System.out.println(hotelDoc);
        }

    }



}
