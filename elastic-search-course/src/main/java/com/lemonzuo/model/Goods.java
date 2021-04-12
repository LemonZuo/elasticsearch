package com.lemonzuo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LemonZuo
 * @create 2021-04-11 22:49
 * 京东商品
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {
    /**
     * String imgSrc = httpsPrefix + element.getElementsByAttribute("data-lazy-img").attr("data-lazy-img");
     *                 String price = element.getElementsByClass("p-price").text();
     *                 String name = element.getElementsByClass("p-name").text();
     *                 String shopName = element.getElementsByClass("p-shopnum").text();
     */
    private String imgSrc;
    private String price;
    private String proName;
    private String shopName;
}
