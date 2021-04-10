package com.lemonzuo.controller;

import com.lemonzuo.spider.Spider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LemonZuo
 * @create 2021-04-10 8:15
 */
@RestController
public class CourseController {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @GetMapping("start")
    public String start() throws IOException {
        Spider spider = new Spider();
        spider.main();
        return "SUCCESS";
    }

    public List searchPage(String keyWords, int pageNo, int pageSize) {
        List list = new ArrayList();
        String index = "db_course";
        SearchRequest searchRequest = new SearchRequest(index);

        return list;
    }
}
