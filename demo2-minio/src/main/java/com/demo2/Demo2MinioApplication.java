package com.demo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(value = "com.demo2.config")
public class Demo2MinioApplication {

	public static void main(String[] args) {
		SpringApplication.run(Demo2MinioApplication.class, args);
	}

}
