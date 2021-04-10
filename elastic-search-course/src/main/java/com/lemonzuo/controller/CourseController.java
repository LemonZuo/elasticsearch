package com.lemonzuo.controller;

import com.lemonzuo.spider.Spider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author LemonZuo
 * @create 2021-04-10 8:15
 */
@RestController
public class CourseController {
    @Resource(name = "restHighLevelClient")
    private RestHighLevelClient client;
    @GetMapping("start")
    public String start() throws IOException {
        Spider spider = new Spider();
        spider.main();
        return "SUCCESS";
    }

    /**
     * 非高亮分页查询
     * @param keywords 检索关键字
     * @param pageNo 当前页号
     * @param pageSize 每页数据条数
     * @return
     * @throws IOException
     */
    @GetMapping("searchPage/{keywords}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPage(@PathVariable String keywords, @PathVariable int pageNo, @PathVariable int pageSize) throws IOException {
        List<Map<String, Object>> list = new ArrayList();
        String index = "db_course";
        // 搜索请求
        SearchRequest searchRequest = new SearchRequest(index);
        // 搜索构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 使用精确查询
        TermQueryBuilder termQuery = QueryBuilders.termQuery("courseName", keywords);
        searchSourceBuilder
                .query(termQuery)
                .timeout(new TimeValue(60, TimeUnit.SECONDS))
                // 分页配置
                .from(pageNo <=1?1:pageNo * pageSize)
                .size(pageSize);
        // 搜索请求配置搜索构建器
        searchRequest.source(searchSourceBuilder);

        // 客户端提交搜索请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        searchResponse.getHits().forEach(item -> {
            Map<String, Object> sourceAsMap = item.getSourceAsMap();
            list.add(sourceAsMap);
        });
        return list;
    }

    /**
     * 高亮分页查询
     * @param keywords 检索关键字
     * @param pageNo 当前页号
     * @param pageSize 每页数据条数
     * @return
     * @throws IOException
     */
    @GetMapping("searchPageHighlight/{keywords}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> searchPageHighlight(@PathVariable String keywords,
                                                         @PathVariable int pageNo,
                                                         @PathVariable int pageSize) throws IOException {
        List<Map<String, Object>> list = new ArrayList();
        String index = "db_course";
        // 搜索请求
        SearchRequest searchRequest = new SearchRequest(index);
        // 搜索构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 使用精确查询
        TermQueryBuilder termQuery = QueryBuilders.termQuery("courseName", keywords);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("courseName").preTags("<p style='font-color:red'>").postTags("</>");
        // 高亮查询配置
        searchSourceBuilder
                .query(termQuery)
                .highlighter(highlightBuilder)
                .timeout(new TimeValue(60, TimeUnit.SECONDS))
                // 分页配置
                .from(pageNo <=1?1:pageNo * pageSize)
                .size(pageSize);
        // 搜索请求配置搜索构建器
        searchRequest.source(searchSourceBuilder);

        // 客户端提交搜索请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            // 获取检索数据
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // 取出高亮字段
            HighlightField courseName = highlightFields.get("courseName");
            if(courseName != null) {
                String tempCourseName = "";
                for (Text fragment : courseName.fragments()) {
                    tempCourseName += fragment;
                }
                // 覆盖数据中高亮字段数据
                sourceAsMap.put("courseName", tempCourseName);
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
