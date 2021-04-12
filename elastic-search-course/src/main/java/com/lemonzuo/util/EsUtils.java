package com.lemonzuo.util;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.lemonzuo.model.Course;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * @author LemonZuo
 * @create 2021-04-11 22:53
 */
@Slf4j
public class EsUtils {
    private static volatile RestHighLevelClient client = SpringUtil.getBean("restHighLevelClient", RestHighLevelClient.class);

    public static void batchAdd(List objectList, @NonNull String index) throws IOException {
        // 判断索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            // 创建索引请求
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            // 客户端执行请求，获得响应
            CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }

        // 执行插入
        BulkRequest request = new BulkRequest();
        request.timeout("120s");

        objectList.forEach(item -> {
            IndexRequest indexRequest = new IndexRequest(index)
                    .id(IdUtil.objectId())
                    .source(JSON.toJSONString(item), XContentType.JSON);
            request.add(indexRequest);
        });

        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        log.info("{}", responses.status());
    }
}
