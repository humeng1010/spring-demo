package com.demo3;

import com.alibaba.fastjson.JSON;
import com.demo3.constants.HotelConstants;
import com.demo3.constants.HotelDocField;
import com.demo3.entity.Hotel;
import com.demo3.mapper.HotelEsMapper;
import com.demo3.model.docs.HotelDoc;
import com.demo3.service.HotelService;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
    void testDeleteIndex() {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    void testGetIndex() {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        Assertions.assertTrue(exists);
    }

    @Resource
    private HotelService hotelService;

    /**
     * 把数据库中的数据批量导入到es中
     *
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
     *
     * @throws IOException
     */
    @Test
    void testDelDocumentByIds() throws IOException {
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest("hotel").id("1"));
        request.add(new DeleteRequest("hotel").id("36934"));
        request.add(new DeleteRequest("hotel").id("38609"));
        client.bulk(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDocument() throws IOException {
        //0.查询数据库中的数据
        List<Hotel> list = hotelService.list();
        // 1.创建Request
        BulkRequest request = new BulkRequest();
        for (Hotel hotel : list) {
            // 增量修改
            request.add(new UpdateRequest("hotel", hotel.getId().toString())
                    .doc(
                            "price", "952",
                            "starName", "四钻"
                    ));
        }
        //3.发送请求
        client.bulk(request, RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    void testMatchAll() {
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
    void testMatch() {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("name", "外滩如家").analyzer("ik_max_word"));
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
    void testBoolQuery() {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("city", "上海"))
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
    void testGEO() {
        SearchRequest request = new SearchRequest("hotel");
        request.source().sort(SortBuilders.geoDistanceSort("location", new GeoPoint(31.21, 121.5))
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
    void testElasticSearchTemplate() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", "陆家嘴"))
                .withPageable(PageRequest.of(0, 10))
                .withSort(SortBuilders.geoDistanceSort(HotelDocField.LOCATION, new GeoPoint(31.21, 121.5))
                        .unit(DistanceUnit.KILOMETERS)
                        .order(SortOrder.ASC));

        SearchHits<HotelDoc> search = elasticsearchRestTemplate.search(queryBuilder.build(), HotelDoc.class);
        List<org.springframework.data.elasticsearch.core.SearchHit<HotelDoc>> searchHits = search.getSearchHits();
        for (org.springframework.data.elasticsearch.core.SearchHit<HotelDoc> searchHit : searchHits) {
            HotelDoc content = searchHit.getContent();
            List<Object> sortValues = searchHit.getSortValues();
            for (Object sortValue : sortValues) {
                System.out.println(sortValue + "km");
            }
            System.out.println(content);
        }

    }

    @Resource
    private HotelEsMapper hotelEsMapper;

    @Test
    void testEsMapper() {
        Iterable<HotelDoc> all = hotelEsMapper.findAll();
        for (HotelDoc hotelDoc : all) {
            System.out.println(hotelDoc);
        }

    }

    /**
     * 聚合函数的练习
     *
     * @throws NoSuchFieldException
     */
    @Test
    void testAgg() throws NoSuchFieldException {
        // 统计不同城市的酒店评分情况-适合用户
        // 用户在某个城市
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // if city != null
//        nativeSearchQueryBuilder.withFilter(QueryBuilders.termQuery(HotelDocField.CITY,"上海"));
        // end
        // 否则查询所有城市
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("cityAgg").field(HotelDocField.CITY)
                        .size(20)
                        .order(BucketOrder.count(false))
                        .subAggregation(AggregationBuilders.terms("brandAgg").field(HotelDocField.BRAND)
                                .size(100)
                                .subAggregation(AggregationBuilders.stats("scoreStats").field(HotelDocField.SCORE))
                                .order(BucketOrder.aggregation("scoreStats.avg", false))))
                .withPageable(PageRequest.of(0, 1));
        SearchHits<HotelDoc> search = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), HotelDoc.class);
        Terms cityAgg = Objects.requireNonNull(search.getAggregations()).get("cityAgg");
        List<? extends Terms.Bucket> buckets = cityAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String city = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("城市：" + city + " 酒店数量：" + docCount);
            Terms aggregation = bucket.getAggregations().get("brandAgg");
            List<? extends Terms.Bucket> buckets1 = aggregation.getBuckets();
            for (Terms.Bucket bucket1 : buckets1) {
                String keyAsString = bucket1.getKeyAsString();
                long docCount1 = bucket1.getDocCount();
                ParsedStats aggregation2 = bucket1.getAggregations().get("scoreStats");
                double avg = aggregation2.getAvg();
                System.out.println("品牌：" + keyAsString + " 数量：" + docCount1 + " 平均分：" + avg);
            }
        }
    }

    @Test
    void testAgg2() {
        // 统计不同酒店品牌在不同城市的评分情况-适合酒店管理人
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder
                .addAggregation(AggregationBuilders.terms("brandAgg").field(HotelDocField.BRAND).size(10)
                        .subAggregation(AggregationBuilders.terms("cityAgg").field(HotelDocField.CITY).size(10)
                                .subAggregation(AggregationBuilders.avg("scoreAvg").field(HotelDocField.SCORE))
                                .order(BucketOrder.aggregation("scoreAvg", false))))
//                .withQuery(QueryBuilders.functionScoreQuery(QueryBuilders.matchQuery(HotelDocField.BRAND,"如家"),
//                                ScoreFunctionBuilders.weightFactorFunction(10))
//                        .boostMode(CombineFunction.SUM))// 给如家加10分权重，他给钱了，打广告了
                .withPageable(PageRequest.of(0, 1));
        SearchHits<HotelDoc> search = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), HotelDoc.class);
        Terms aggregation = search.getAggregations().get("brandAgg");
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("品牌：" + keyAsString + " 数量：" + docCount);
            Terms aggregation1 = bucket.getAggregations().get("cityAgg");
            List<? extends Terms.Bucket> buckets1 = aggregation1.getBuckets();
            for (Terms.Bucket bucket1 : buckets1) {
                String city = bucket1.getKeyAsString();
                long count = bucket1.getDocCount();
                ParsedAvg avg = bucket1.getAggregations().get("scoreAvg");
                double value = avg.getValue();
                System.out.println("城市：" + city + " 数量：" + count + " 平均分：" + value);
            }

        }
    }


}
