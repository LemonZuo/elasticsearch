package com.lemonzuo.spider;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.lemonzuo.model.Course;
import com.lemonzuo.util.SpringUtil;
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
import org.jsoup.select.Elements;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LemonZuo
 * @create 2021-04-08 20:42
 */
@Slf4j
public class Spider {
    RestHighLevelClient client = SpringUtil.getBean("restHighLevelClient", RestHighLevelClient.class);

    /**
     * 解析网页
     *
     * @param url
     * @throws IOException
     */
    public void getData(String url) throws IOException {
        List<Course> courseList = new ArrayList<>();
        Document document = Jsoup.parse(new URL(url), 30000);
        Elements elements = document.getElementsByClass("course-item-box");

        elements.forEach(element -> {
            String src = element.getElementsByTag("img").eq(0).attr("src");
            String imgUrl = saveFile(src);
            String courseName = element.getElementsByClass("course-name").eq(0).text();
            String courseDescription = element.getElementsByClass("course-description").eq(0).text();
            String studentsCount = element.getElementsByClass("students-count").eq(0).text();
            String patternStr = "[1-9]\\d*";

            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(studentsCount);
            if (matcher.find()) {
                studentsCount = matcher.group();
            }
            Course course = new Course(imgUrl, courseName, courseDescription, studentsCount);
            courseList.add(course);
        });

        // 存储到ES
        saveToEs(courseList);
    }

    public void saveToEs(List<Course> courseList) throws IOException {
        // 判断索引是否存在
        String index = "db_course";
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

        courseList.forEach(course -> {
            IndexRequest indexRequest = new IndexRequest(index)
                    .id(IdUtil.objectId())
                    .source(JSON.toJSONString(course), XContentType.JSON);
            request.add(indexRequest);
        });

        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        log.info("{}", responses.status());
    }

    /**
     * 解析url下载文件到本地
     *
     * @param src
     */
    public static String saveFile(String src) {
        String filePrefix = "F:\\spider\\img\\";
        String imgUrlPrefix = "http://127.0.0.1:8085/img/";
        RestTemplate restTemplate = new RestTemplate();
        if (src.contains("?")) {
            src = src.substring(0, src.lastIndexOf("?"));
        }
        ResponseEntity<Resource> imgResponse = restTemplate.exchange(src, HttpMethod.GET, null, Resource.class);
        HttpHeaders headers = imgResponse.getHeaders();
        String subtype = headers.getContentType().getSubtype();
        String tag = headers.getETag().replace("\"", "");
        String fileName = String.join("", filePrefix, tag, ".", subtype);
        String imgUrl = String.join("", imgUrlPrefix, tag, ".", subtype);

        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream out = null;
        InputStream input = null;
        try {
            input = imgResponse.getBody().getInputStream();
            out = new FileOutputStream(file);
            byte[] buff = new byte[1024 * 10];
            int index = 0;
            // 4、执行 写出操作
            while ((index = input.read(buff)) != -1) {
                out.write(buff, 0, index);
                log.info("{}", "下载中>>>>>>>>>>");
                out.flush();
            }
            return imgUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return imgUrl;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void main() throws IOException {
        int end = 32;
        String baseUrl = "https://www.lanqiao.cn/courses/?page=";
        for (int i = 1; i < end; i++) {
            String url = baseUrl + i;
            getData(url);
        }

    }
}
