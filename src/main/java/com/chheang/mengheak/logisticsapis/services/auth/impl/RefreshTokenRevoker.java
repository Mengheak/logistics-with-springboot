package com.chheang.mengheak.logisticsapis.services.auth.impl;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Revocations that have to outlive a rejected request.
 * <p>
 * Detecting a replayed token means revoking tokens <em>and</em> failing the call. Doing both in
 * one transaction cannot work: the exception rolls the revocation back, so the stolen token would
 * stay alive. These run in their own transaction (REQUIRES_NEW) and commit independently of the
 * caller's rollback.
 * <p>
 * It lives in a separate bean because {@code REQUIRES_NEW} is applied by the Spring proxy, and a
 * method calling a sibling on {@code this} would bypass it.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeFamilyNow(UUID familyId, TokenRevocationReason reason) {
        return refreshTokenRepository.revokeFamily(familyId, reason, Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeAllForUserNow(UUID userId, TokenRevocationReason reason) {
        return refreshTokenRepository.revokeAllForUser(userId, reason, Instant.now());
    }
}
