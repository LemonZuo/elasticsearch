package com.lemonzuo.elasticsearch.api;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author LemonZuo
 * @create 2021-04-03 20:45
 */
@Slf4j
@RestController
public class ElasticSearchIndexApi {

    @Qualifier("restHighLevelClient")
    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建索引
     * @param str
     * @throws IOException
     */
    @GetMapping("createIndex")
    public void createIndex(String str) throws IOException {
        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(str);
        // 客户端执行请求，获得响应
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        log.info("{}", response);
    }

    /**
     * 判断索引是否存在
     * @param index
     * @return
     * @throws IOException
     */
    @GetMapping("indexIsExists/{index}")
    public boolean indexIsExists(@PathVariable String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        return exists;
    }

    @GetMapping("deleteIndex/{index}")
    public void deleteIndex(@PathVariable String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        if(exists) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
            AcknowledgedResponse response = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            boolean responseAcknowledged = response.isAcknowledged();
            if (responseAcknowledged) {
                log.info("{}", "索引删除成功");
            } else {
                log.info("{}", "索引删除失败");
            }
        } else {
            log.info("{}", "索引不存在");
        }

    }
}
