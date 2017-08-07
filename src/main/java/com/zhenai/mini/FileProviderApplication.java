package com.zhenai.mini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FileProviderApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileProviderApplication.class, args);
	}
}
