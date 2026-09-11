package com.chheang.mengheak.logisticsapis.bootstrap;

import com.chheang.mengheak.logisticsapis.common.enums.RoleName;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.repositories.RoleRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds the four roles the authorization rules reference, plus a first admin so a fresh H2
 * database is usable straight away. Disabled in prod via {@code config.bootstrap.seed-enabled}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationConfig applicationConfig;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ApplicationConfig.Bootstrap bootstrap = applicationConfig.getBootstrap();
        if (!bootstrap.isSeedEnabled()) {
            return;
        }

        seedRoles();
        seedAdmin(bootstrap);
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName.name())) {
                roleRepository.save(Role.builder()
                        .name(roleName.name())
                        .description(describe(roleName))
                        .build());
                log.info("Seeded role {}", roleName);
            }
        }
    }

    private void seedAdmin(ApplicationConfig.Bootstrap bootstrap) {
        String email = bootstrap.getAdminEmail();
        if (email == null || bootstrap.getAdminPassword() == null) {
            return;
        }
        if (userAccountRepository.existsByEmail(email)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN.name()).orElseThrow();
        userAccountRepository.save(UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(bootstrap.getAdminPassword()))
                .fullName("Platform Administrator")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build());

        log.warn("Seeded bootstrap admin {} - change this password before any real use", email);
    }

    private String describe(RoleName roleName) {
        return switch (roleName) {
            case ADMIN -> "Full access to every resource";
            case DISPATCHER -> "Books shipments and plans trips";
            case DRIVER -> "Records scans from the field";
            case CUSTOMER -> "Self-service access for a booking party";
        };
    }
}
