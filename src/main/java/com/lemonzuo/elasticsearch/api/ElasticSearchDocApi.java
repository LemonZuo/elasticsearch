package com.lemonzuo.elasticsearch.api;

import com.alibaba.fastjson.JSON;
import com.lemonzuo.elasticsearch.model.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @author LemonZuo
 * @create 2021-04-03 21:54
 */
@Slf4j
@RestController
public class ElasticSearchDocApi {
    @Qualifier("restHighLevelClient")
    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建文档
     * @throws IOException
     */
    @GetMapping("createDoc")
    public void createDoc() throws IOException {
        // 创建对象
        User user = new User("Lemon", 18);
        // 创建请求
        IndexRequest indexRequest = new IndexRequest("db_api");
        // PUT db_api/_doc/1
        // 设置文档ID
        indexRequest.id("1");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        // 将数据放入请求
        IndexRequest docRequest = indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        // 客户端发送请求获取响应结果
        IndexResponse indexResponse = client.index(docRequest, RequestOptions.DEFAULT);
        log.info("{}", indexResponse.toString());
        log.info("{}", indexResponse.status());
    }

    /**
     * 文档是否存在
     * @throws IOException
     */
    @GetMapping("docIsExists")
    public void docIsExists() throws IOException {
        GetRequest request = new GetRequest("db_api", "1");
        // 不获取返回的_source上下文
        request.fetchSourceContext();
        // 排序
        request.storedFields();
        boolean exists = client.exists(request, RequestOptions.DEFAULT);

        log.info("{}", exists);
    }

    /**
     * 获取文档信息
     * @throws IOException
     */
    @GetMapping("getDoc")
    public void getDoc() throws IOException {
        GetRequest request = new GetRequest("db_api", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String source = response.getSourceAsString();
        log.info("{}", source);
    }

    /**
     * 更新文档
     * @throws IOException
     */
    @GetMapping("updateDoc")
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("db_api", "1");

        User user = new User("LemonZuo", 18);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        log.info("{}", updateResponse.status());

    }

    /**
     * 删除文档
     * @throws IOException
     */
    @GetMapping("deleteDoc")
    public void deleteDoc() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("db_api", "1");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        log.info("{}", deleteResponse.status());
    }
}
