package com.ftnam.image_ai_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@ComponentScan(basePackages = "com.ftnam.image_ai_backend")
@EnableFeignClients
@EnableScheduling
public class ImageAiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageAiBackendApplication.class, args);
	}

}
