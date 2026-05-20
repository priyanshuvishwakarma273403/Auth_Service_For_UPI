package com.authService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auth Service - Main Application
 *
// * @EnableDiscoveryClient -> Eureka mein register hoga
// * @EnableScheduling      -> Expired refresh token cleanup scheduler ke liye
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
