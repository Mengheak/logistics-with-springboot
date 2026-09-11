package com.chheang.mengheak.logisticsapis.services.auth;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.entities.auth.RefreshToken;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;

public interface RefreshTokenService {

    /** Starts a new session: a fresh rotation family for this login. */
    IssuedRefreshToken issueForNewSession(UserAccount user, String clientInfo);

    /**
     * Consumes {@code rawToken} and issues its replacement inside the same family. Throws if the
     * token is unknown, expired, or already used - and in the last case revokes the whole family,
     * because a second use means a copy is in someone else's hands.
     */
    IssuedRefreshToken rotate(String rawToken, String clientInfo);

    /** Single-session logout. Quietly does nothing for a token that is already unusable. */
    void revoke(String rawToken);

    /** Signs the owner of {@code rawToken} out of every device. Returns how many were revoked. */
    int revokeAllSessions(String rawToken);

    /** Revokes every active token for a user, e.g. when an admin disables the account. */
    int revokeAllSessionsForUser(UserAccount user, TokenRevocationReason reason);

    int purgeExpired();

    long refreshValiditySeconds();

    /**
     * A newly minted token: the persisted row plus the raw value, which exists only here and in
     * the response built from it.
     */
    record IssuedRefreshToken(RefreshToken token, String rawValue) {
    }
}
