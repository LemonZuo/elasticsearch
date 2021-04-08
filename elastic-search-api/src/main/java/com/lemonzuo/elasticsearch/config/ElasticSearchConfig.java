package com.lemonzuo.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author LemonZuo
 * @create 2021-04-03 20:31
 */
@Configuration
public class ElasticSearchConfig {
    @Bean("restHighLevelClient")
    public RestHighLevelClient getRestHighLevelClient() {
        HttpHost host = new HttpHost("127.0.0.1", 9200, "http");
        RestClientBuilder clientBuilder = RestClient.builder(host);
        RestHighLevelClient client = new RestHighLevelClient(clientBuilder);
        return client;
    }
}
