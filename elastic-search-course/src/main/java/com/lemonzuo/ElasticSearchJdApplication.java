package com.lemonzuo;

import com.lemonzuo.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 左铠琦
 */
@SpringBootApplication
public class ElasticSearchJdApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ElasticSearchJdApplication.class, args);
        SpringUtil.set(context);
    }

}
