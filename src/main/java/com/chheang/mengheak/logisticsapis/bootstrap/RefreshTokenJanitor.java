package com.chheang.mengheak.logisticsapis.bootstrap;

import com.chheang.mengheak.logisticsapis.services.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Expired refresh tokens are dead weight: without this the table only ever grows, one row per
 * refresh per user. Runs nightly, well outside the busy dispatch window.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenJanitor {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 30 3 * * *")
    public void purgeExpiredTokens() {
        int purged = refreshTokenService.purgeExpired();
        if (purged > 0) {
            log.info("Purged {} expired refresh token(s)", purged);
        }
    }
}
