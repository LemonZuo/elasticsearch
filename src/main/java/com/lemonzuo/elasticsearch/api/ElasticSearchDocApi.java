package com.lemonzuo.elasticsearch.api;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LemonZuo
 * @create 2021-04-03 21:54
 */
@RestController
public class ElasticSearchDocApi {
    @Qualifier("restHighLevelClient")
    @Autowired
    private RestHighLevelClient client;
}
