package com.lemonzuo.spider;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.lemonzuo.model.ProxyInfo;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LemonZuo
 * @create 2021-04-10 21:55
 * 快代理代理爬虫
 */
@Slf4j
@RestController
public class KuaiDaiLiProxySpider {
    @Resource
    RestHighLevelClient client;

    @GetMapping("getProxy")
    public void getProxy() throws IOException {
        String baseUrl = "https://www.kuaidaili.com/free/";
        // Document document = Jsoup.parse(new URL(baseUrl), 30 * 1000);
        // 27.43.186.21:9999
        Document document = Jsoup.connect(baseUrl).proxy("58.218.200.225", 4472).timeout(60 * 1000).get();
        Elements elements = document.getElementById("listnav").getElementsByTag("a");
        Element last = elements.last();
        int maxPageNo = 0;
        if (last.html() != null && last.html() != null) {
            maxPageNo = Integer.parseInt(last.html());
        }
        if (maxPageNo <= 0) {
            return;
        }
        for (int i = 1; i <= maxPageNo; i++) {
            try {
                dealPage(i);
                Thread.sleep(5 * 1000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void dealPage(int pageNo) throws IOException, InterruptedException {
        List<ProxyInfo> proxyInfoList = new ArrayList<>();
        URL url = new URL("https://www.kuaidaili.com/free/inha/".concat(String.valueOf(pageNo)));
        Thread.sleep(2 * 1000);
        Document document = Jsoup.parse(url, 5 * 1000);
        Elements elements = document.getElementsByTag("tr");
        log.info("{}", elements.size());
        elements.remove(0);
        log.info("{}", elements.size());
        elements.forEach(element -> {
            String ip = element.getElementsByAttributeValue("data-title", "IP").text();
            log.info("{}", ip);
            String port = element.getElementsByAttributeValue("data-title", "PORT").text();
            log.info("{}", port);
            proxyInfoList.add(new ProxyInfo(ip, port));
        });
        saveToEs(proxyInfoList);
    }

    public void saveToEs(List<ProxyInfo> proxyInfoList) throws IOException {
        // 判断索引是否存在
        String index = "db_proxy";
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

        proxyInfoList.forEach(proxyInfo -> {
            IndexRequest indexRequest = new IndexRequest(index)
                    .id(IdUtil.objectId())
                    .source(JSON.toJSONString(proxyInfo), XContentType.JSON);
            request.add(indexRequest);
        });

        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        log.info("{}", responses.status());
    }
}
