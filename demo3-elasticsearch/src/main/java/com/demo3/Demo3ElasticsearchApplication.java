package com.demo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Demo3ElasticsearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(Demo3ElasticsearchApplication.class, args);
    }

}
