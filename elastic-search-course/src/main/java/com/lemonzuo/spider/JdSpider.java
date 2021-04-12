package com.lemonzuo.spider;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.lemonzuo.model.Goods;
import com.lemonzuo.util.EsUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LemonZuo
 * @create 2021-04-11 13:42
 * 京东爬虫
 */
@Slf4j
@RestController
public class JdSpider {
    @Resource
    private RestHighLevelClient client;
    private static int lastFailPageNo = 1;

    /**
     * 当前使用代理节点
     */
    private volatile ZhiMaProxyInfo currentProxy = ZhiMaProxyUtil.getProxy();

    @GetMapping("jdSpider/{keyword}")
    public void jdSpider(@PathVariable String keyword) throws InterruptedException, IOException {
        String index = "db_jd_goods";
        // 通过代理获取总页数
        int maxPage = 0;
        // 代理失效
        if (currentProxy == null || new Date().after(currentProxy.getExpire_time())) {
            currentProxy = ZhiMaProxyUtil.getProxy();
        }
        String agent = "Mozilla/5.0 (Macintosh) AppleWebKit/67.90 (KHTML, like Gecko) Edge/14.18037 Safari/525.71";
        // 获取总页数
        Document document = null;
        try {
            log.info("开始获取总页数===>");
            document = Jsoup.connect("https://search.jd.com/Search?keyword="+keyword)
                    .proxy(currentProxy.getIp(), currentProxy.getPort())
                    .userAgent(agent)
                    .timeout(30 * 1000).get();
            log.info("总页数获取成功===>");
        } catch (IOException e) {
            e.printStackTrace();
            currentProxy = ZhiMaProxyUtil.getProxy();
            HttpRequest.get("http://127.0.0.1:9000/jdSpider").execute();
        }
        String total = document.getElementById("J_topPage").getElementsByTag("i").eq(0).text();
        if (total != null && !total.equals("")) {
            maxPage = Integer.parseInt(total);
        }
        // 页数非法，停止爬取
        if (maxPage < 1) {
            return;
        }
        // 随机休眠继续获取数据
        TimeUnit.SECONDS.sleep(RandomUtil.randomInt(1, 10));
        // 遍历爬取数据
        int i = 1;
        for (i = (lastFailPageNo > 1 ? lastFailPageNo : i); i <= maxPage; i++) {
            List<Goods> goodsList = new ArrayList<>();
            if (currentProxy == null || new Date().after(currentProxy.getExpire_time())) {
                currentProxy = ZhiMaProxyUtil.getProxy();
            }
            int randomSleepTime = RandomUtil.randomInt(1, 10);
            int randomUserAgent = RandomUtil.randomInt(1, 10000);
            String userAgent = getUserAgent(randomUserAgent);
            if (userAgent == null || "".equals(userAgent)) {
                userAgent = "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/18.64 (KHTML, like Gecko) Chrome/69.3.1279.388 " +
                        "Safari/198.85";
            }

            try {
                log.info("数据爬取开始====>");
                document = Jsoup.connect("https://search.jd.com/Search?keyword=java&page=" + i)
                        .proxy(currentProxy.getIp(), currentProxy.getPort())
                        .userAgent(userAgent)
                        .timeout(30 * 1000).get();
                log.info("数据爬取成功====>");
            } catch (IOException e) {
                log.error("当前处理页码为：{}", i);
                e.printStackTrace();
                lastFailPageNo = i;
                currentProxy = ZhiMaProxyUtil.getProxy();
                continue;
            }
            Elements elements = document.getElementsByClass("gl-i-wrap");
            String floatPattern = "[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*";
            String httpsPrefix = "https:";
            elements.forEach(element -> {
                String imgSrc = httpsPrefix + element.getElementsByAttribute("data-lazy-img").attr("data-lazy-img");
                String price = element.getElementsByClass("p-price").text();
                String proName = element.getElementsByClass("p-name").text();
                String shopName = element.getElementsByClass("p-shopnum").text();
                Pattern pattern = Pattern.compile(floatPattern);
                Matcher matcher = pattern.matcher(price);
                if (matcher.find()) {
                    price = matcher.group();
                }
                Goods goods = new Goods(imgSrc, price, proName, shopName);
                goodsList.add(goods);
            });
            EsUtils.batchAdd(goodsList, index);
            TimeUnit.SECONDS.sleep(randomSleepTime);
        }
    }

//    @GetMapping("getZhiMaProxy")
//    public void getZhiMaProxy() throws InterruptedException, IOException {
//        JdSpider jdSpider = new JdSpider();
//        jdSpider.jdSpider();
//    }

    public String getUserAgent(int id) {
        try {
            GetRequest request = new GetRequest("db_user_agent", String.valueOf(id));
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            return response.getSourceAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
