package com.laitsneo.whitelbl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.laitsneo.whitelbl")
public class WhitelblApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhitelblApplication.class, args);
	}

}
