package com.laitsneo.mipPay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.laitsneo.whitelbl")
public class MippayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MippayApplication.class, args);
	}

}
