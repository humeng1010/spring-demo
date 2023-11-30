package com.demo4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = {
        "com.demo4",
        "com.demo5"
})
public class Demo4AopLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(Demo4AopLogApplication.class, args);
    }

}
