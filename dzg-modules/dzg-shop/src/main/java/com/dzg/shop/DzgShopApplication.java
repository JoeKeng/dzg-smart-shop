package com.dzg.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 店铺经营模块
 */
@SpringBootApplication
public class DzgShopApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DzgShopApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("店掌柜店铺经营模块启动成功");
    }
}
