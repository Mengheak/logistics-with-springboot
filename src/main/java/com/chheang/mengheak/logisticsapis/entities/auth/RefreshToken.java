package com.chheang.mengheak.logisticsapis.entities.auth;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * A persisted, opaque refresh token. Deliberately not a JWT: a refresh token has to be
 * revocable, and a self-contained token cannot be taken back before it expires.
 * <p>
 * Only the SHA-256 hash is stored, for the same reason passwords are hashed - a dump of this
 * table must not hand an attacker usable credentials. The raw value exists once, in the
 * response that issued it.
 * <p>
 * Tokens issued by rotating an earlier one share its {@code familyId}, so detecting a replayed
 * token lets the whole chain be revoked at once.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_refresh_tokens_family", columnList = "family_id"),
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RefreshToken {

    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id")
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    /** Shared by every token in one rotation chain, i.e. one login session. */
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason", length = 30)
    private TokenRevocationReason revocationReason;

    /** Best-effort client fingerprint, to make a session list meaningful to the user. */
    @Column(name = "client_info", length = 200)
    private String clientInfo;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isUsable(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(TokenRevocationReason reason, Instant now) {
        this.revokedAt = now;
        this.revocationReason = reason;
    }
}
