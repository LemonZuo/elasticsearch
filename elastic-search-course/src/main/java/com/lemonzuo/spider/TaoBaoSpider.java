package com.lemonzuo.spider;

import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LemonZuo
 * @create 2021-04-12 22:32
 * 淘宝爬虫
 */
@Slf4j
@RestController
public class TaoBaoSpider {
    private final static int PAGE_SIZE = 44;
    private volatile int lastFailPageNo = 0;
    private volatile String lastFailUrl = "";
    private volatile ZhiMaProxyInfo currentProxy = ZhiMaProxyUtil.getProxy();
    private final static  String BASE_URL = "https://s.taobao.com/search";

    @GetMapping("taoBaoSpider/{keyword}")
    public String taoBaoSpider(@PathVariable String keyword) {
        String index = "db_tao_bao_goods";
        String url = BASE_URL.concat("?q=").concat(keyword);
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "_m_h5_tk=7cac502b50333b35adf859a88e057573_1618245205722; _m_h5_tk_enc=ae7dc453f81d6c2e410a21c235ad1b10; cookie2=169bd32a66c6276c4fc19c794f30dcbd; t=f989a54d97511864f6cb4edcb3e51bba; _tb_token_=5ed8ee6b584e3; xlly_s=1; _samesite_flag_=true; enc=tMoyUM5EGYYI%2BzIBcTeRqHCjA0qc1PcKJ%2B3rCyzlTqW7rPRg1s9S6m5KSYsOaJjpC%2BwB1ZkDI0v2gJcTav8BEg%3D%3D; thw=cn; hng=CN%7Czh-CN%7CCNY%7C156; mt=ci=0_0; tracknick=; cna=v1zzGBDYFGoCATsqeeDx5Rsh; alitrackid=www.taobao.com; lastalitrackid=www.taobao.com; JSESSIONID=1141AF6D3B03F2EE4C70BAB1F8789CA2; isg=BPb2HQJeijXndH7RxypYw8tHRyz4FzpRDbqENmDf4ll0o5Y9yKeKYVxRu3_PEDJp; l=eBjZedX4j1_PSwOLBOfanurza77OSIRYYuPzaNbMiOCPOJCB5a85W6a-xq86C3GVh6fyR38KdX6JBeYBqQAonxv92j-la_kmn; tfstk=c1L1BFwdXR26Eoo47CGUubEchp_Aw2UCtyXe13_rk9-laT1clzWSmZC_dyIRO");
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
            document = Jsoup.connect(url)
                    .proxy(currentProxy.getIp(), currentProxy.getPort())
                    .headers(headers)
                    .userAgent(agent)
                    .timeout(30 * 1000).get();
            log.info("总页数获取成功===>");
        } catch (IOException e) {
            e.printStackTrace();
            currentProxy = ZhiMaProxyUtil.getProxy();
            HttpRequest.get("http://127.0.0.1:9000/taoBaoSpider/".concat(keyword)).execute();
        }
        String text = document.select("div#mainsrp-pager>.m-page>.wraper>.inner>.total").eq(0).text();
        return text;
    }

}
