package com.lemonzuo.spider;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LemonZuo
 * @create 2021-04-11 10:31
 */
@Slf4j
@RestController
public class ZhiMaProxyUtil {
    private static final String API_URL = "http://webapi.http.zhimacangku.com/getip";
    private static final String FREE = "141888";
    private static final String DAY = "141999";
    private static final String MONTH = "142002";
    private static Map<String, Object> param = new HashMap<>(15);

    static {
        param.put("num", "1");
        param.put("type", "2");
        param.put("pro", "");
        param.put("city", "0");
        param.put("yys", "0");
        param.put("port", "1");

        param.put("ts", "1");
        param.put("ys", "1");
        param.put("cs", "1");
        param.put("lb", "1");
        param.put("sb", "0");
        param.put("pb", "45");
        param.put("mr", "1");
        param.put("regions", "");
    }

    public static ZhiMaProxyInfo getProxy() {
        // 随机切换套餐
        int setMeal = RandomUtil.randomInt(1, 3);
        switch (setMeal) {
            case 1:
                param.put("pack", FREE);
                break;
            case 2:
                param.put("pack", DAY);
                break;
            case 3:
                param.put("pack", MONTH);
                break;
            default:
                param.put("pack", MONTH);
        }
        String body = null;
        try {
            body = HttpRequest.get(API_URL).form(param).execute().body();
        } catch (HttpException e) {
            e.printStackTrace();
            return null;
        }
        Result<List<ZhiMaProxyInfo>> result = JSON.parseObject(body,
                new TypeReference<Result<List<ZhiMaProxyInfo>>>() {
                });
        if (result.isSuccess()) {
            return result.getData().get(0);
        } else {
            return null;
        }
    }
}
