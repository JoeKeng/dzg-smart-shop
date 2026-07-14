package com.dzg.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 网关启动程序
 *
 * @author Lion Li
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DzgGatewayMvcApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DzgGatewayMvcApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  MVC网关启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
