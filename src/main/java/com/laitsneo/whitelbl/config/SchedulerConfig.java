package com.laitsneo.whitelbl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling  // ← Must be present
public class SchedulerConfig {
   
	// Scheduling is now enabled for the application
}