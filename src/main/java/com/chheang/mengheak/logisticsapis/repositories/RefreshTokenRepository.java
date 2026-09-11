package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.entities.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long countByUserAccountIdAndRevokedAtIsNull(UUID userId);

    /** Kills a whole rotation chain in one statement - the response to a replayed token. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshToken t
               set t.revokedAt = :now, t.revocationReason = :reason
             where t.familyId = :familyId
               and t.revokedAt is null
            """)
    int revokeFamily(@Param("familyId") UUID familyId,
                     @Param("reason") TokenRevocationReason reason,
                     @Param("now") Instant now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshToken t
               set t.revokedAt = :now, t.revocationReason = :reason
             where t.userAccount.id = :userId
               and t.revokedAt is null
            """)
    int revokeAllForUser(@Param("userId") UUID userId,
                         @Param("reason") TokenRevocationReason reason,
                         @Param("now") Instant now);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RefreshToken t where t.userAccount.id = :userId")
    int deleteAllForUser(@Param("userId") UUID userId);
}
