package com.chheang.mengheak.logisticsapis.services.auth.impl;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.entities.auth.RefreshToken;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.UnauthorizedException;
import com.chheang.mengheak.logisticsapis.repositories.RefreshTokenRepository;
import com.chheang.mengheak.logisticsapis.services.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    /** 256 bits of entropy, so the token is unguessable without needing a slow hash. */
    private static final int TOKEN_BYTES = 32;

    /** Revoked rows are kept this long after expiry so reuse is still detectable for a while. */
    private static final Duration PURGE_GRACE = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final ApplicationConfig applicationConfig;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public IssuedRefreshToken issueForNewSession(UserAccount user, String clientInfo) {
        return issue(user, UUID.randomUUID(), clientInfo);
    }

    @Override
    public IssuedRefreshToken rotate(String rawToken, String clientInfo) {
        Instant now = Instant.now();
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (current.isRevoked()) {
            // Only a ROTATED token signals an attack: it was spent legitimately, so a second
            // presentation means a copy leaked. Tokens killed by a logout or a disabled account are
            // just stale clients, and treating those as a breach would raise false alarms.
            if (current.getRevocationReason() == TokenRevocationReason.ROTATED) {
                // The legitimate client's current token lives in the same family, so dropping the
                // family signs the attacker and the victim out together - the victim can sign in
                // again, the attacker cannot. Committed separately, because the exception below
                // would otherwise roll the revocation back.
                int revoked = refreshTokenRevoker.revokeFamilyNow(
                        current.getFamilyId(), TokenRevocationReason.REUSE_DETECTED);
                log.warn("Refresh token replay detected for user {} (family {}), revoked {} token(s)",
                        current.getUserAccount().getId(), current.getFamilyId(), revoked);
                throw new UnauthorizedException(
                        "This refresh token has already been used, every session for this account has been signed out");
            }
            throw new UnauthorizedException("This session has ended, please sign in again");
        }

        if (current.isExpired(now)) {
            throw new UnauthorizedException("Refresh token has expired, please sign in again");
        }

        UserAccount user = current.getUserAccount();
        if (!user.isEnabled()) {
            refreshTokenRevoker.revokeAllForUserNow(
                    user.getId(), TokenRevocationReason.ACCOUNT_DISABLED);
            throw new UnauthorizedException("This account is disabled");
        }

        current.revoke(TokenRevocationReason.ROTATED, now);
        refreshTokenRepository.save(current);

        return issue(user, current.getFamilyId(), clientInfo == null ? current.getClientInfo() : clientInfo);
    }

    /**
     * Logout is intentionally silent about unknown tokens: answering differently would let a
     * caller probe which tokens exist.
     */
    @Override
    public void revoke(String rawToken) {
        Instant now = Instant.now();
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(hash(rawToken));
        found.filter(token -> token.isUsable(now))
                .ifPresent(token -> {
                    token.revoke(TokenRevocationReason.LOGOUT, now);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public int revokeAllSessions(String rawToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        return refreshTokenRepository.revokeAllForUser(
                current.getUserAccount().getId(), TokenRevocationReason.LOGOUT_ALL, Instant.now());
    }

    @Override
    public int revokeAllSessionsForUser(UserAccount user, TokenRevocationReason reason) {
        return refreshTokenRepository.revokeAllForUser(user.getId(), reason, Instant.now());
    }

    @Override
    public int purgeExpired() {
        return refreshTokenRepository.deleteExpiredBefore(Instant.now().minus(PURGE_GRACE));
    }

    @Override
    public long refreshValiditySeconds() {
        return Duration.ofDays(refreshDays()).toSeconds();
    }

    private IssuedRefreshToken issue(UserAccount user, UUID familyId, String clientInfo) {
        Instant now = Instant.now();
        String rawValue = generateRawToken();

        RefreshToken token = refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hash(rawValue))
                .userAccount(user)
                .familyId(familyId)
                .issuedAt(now)
                .expiresAt(now.plus(refreshDays(), ChronoUnit.DAYS))
                .clientInfo(truncate(clientInfo))
                .build());

        return new IssuedRefreshToken(token, rawValue);
    }

    private int refreshDays() {
        return applicationConfig.getSecurity().getJwt().getRefreshTokenDays();
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 rather than BCrypt: the token already has full entropy, so there is nothing to
     * brute-force, and a plain digest is what makes an indexed lookup by hash possible.
     */
    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }

    private String truncate(String clientInfo) {
        if (clientInfo == null || clientInfo.isBlank()) {
            return null;
        }
        return clientInfo.length() <= 200 ? clientInfo : clientInfo.substring(0, 200);
    }
}
