package com.ftnam.image_ai_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ftnam.image_ai_backend")
public class ImageAiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageAiBackendApplication.class, args);
	}

}
