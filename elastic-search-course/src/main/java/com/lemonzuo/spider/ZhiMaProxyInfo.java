package com.lemonzuo.spider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author LemonZuo
 * @create 2021-04-11 10:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhiMaProxyInfo {
    private String ip;
    private int port;
    private Date expire_time;
    private String city;
    private String isp;
}
