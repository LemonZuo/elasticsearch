package com.lemonzuo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LemonZuo
 * @create 2021-04-10 23:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyInfo {
    private String ip;
    private String port;
}
