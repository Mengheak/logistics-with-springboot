package com.chheang.mengheak.logisticsapis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the housekeeping jobs, currently just the expired refresh token purge. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
