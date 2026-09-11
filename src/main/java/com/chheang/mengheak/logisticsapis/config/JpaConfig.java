package com.chheang.mengheak.logisticsapis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Switches on the {@code @CreatedDate} / {@code @LastModifiedDate} stamps on AuditableEntity. */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
