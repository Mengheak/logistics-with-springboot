package com.chheang.mengheak.logisticsapis.services.auth;

import com.chheang.mengheak.logisticsapis.common.enums.RoleName;
import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.entities.auth.RefreshToken;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.UnauthorizedException;
import com.chheang.mengheak.logisticsapis.repositories.RefreshTokenRepository;
import com.chheang.mengheak.logisticsapis.repositories.RoleRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises rotation and replay handling against the real database, because the behaviour that
 * matters here is what the persisted rows look like afterwards.
 * <p>
 * Deliberately <em>not</em> {@code @Transactional}: replay handling revokes in its own
 * transaction (see {@code RefreshTokenRevoker}), which a rolled-back test transaction would hide.
 * Each test works on its own freshly created account instead of relying on rollback.
 */
@SpringBootTest
class RefreshTokenServiceImplTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName(RoleName.CUSTOMER.name()).orElseThrow();
        user = userAccountRepository.save(UserAccount.builder()
                .email("refresh-%s@test.local".formatted(UUID.randomUUID()))
                .passwordHash("irrelevant")
                .fullName("Refresh Tester")
                .enabled(true)
                .roles(Set.of(role))
                .build());
    }

    @Test
    void issuesAnOpaqueTokenAndStoresOnlyItsHash() {
        var issued = refreshTokenService.issueForNewSession(user, "JUnit");

        assertThat(issued.rawValue()).isNotBlank();
        assertThat(issued.token().getTokenHash())
                .hasSize(64)
                .isNotEqualTo(issued.rawValue());
        assertThat(issued.token().isUsable(Instant.now())).isTrue();
        assertThat(issued.token().getClientInfo()).isEqualTo("JUnit");
    }

    @Test
    void rotationReturnsANewTokenAndRetiresTheOldOneInTheSameFamily() {
        var first = refreshTokenService.issueForNewSession(user, "JUnit");
        var second = refreshTokenService.rotate(first.rawValue(), "JUnit");

        assertThat(second.rawValue()).isNotEqualTo(first.rawValue());
        assertThat(second.token().getFamilyId()).isEqualTo(first.token().getFamilyId());

        RefreshToken consumed = refreshTokenRepository.findById(first.token().getId()).orElseThrow();
        assertThat(consumed.isRevoked()).isTrue();
        assertThat(consumed.getRevocationReason()).isEqualTo(TokenRevocationReason.ROTATED);
        assertThat(second.token().isUsable(Instant.now())).isTrue();
    }

    @Test
    void replayingAConsumedTokenRevokesTheWholeFamily() {
        var first = refreshTokenService.issueForNewSession(user, "JUnit");
        var second = refreshTokenService.rotate(first.rawValue(), "JUnit");

        // the attacker replays the token the legitimate client already spent
        assertThatThrownBy(() -> refreshTokenService.rotate(first.rawValue(), "attacker"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("already been used");

        // the legitimate client's current token is gone too - nobody keeps access
        RefreshToken live = refreshTokenRepository.findById(second.token().getId()).orElseThrow();
        assertThat(live.isRevoked()).isTrue();
        assertThat(live.getRevocationReason()).isEqualTo(TokenRevocationReason.REUSE_DETECTED);
        assertThatThrownBy(() -> refreshTokenService.rotate(second.rawValue(), "client"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void aTokenKilledByLogoutIsStaleRatherThanAReplay() {
        var phone = refreshTokenService.issueForNewSession(user, "phone");
        var laptop = refreshTokenService.issueForNewSession(user, "laptop");
        refreshTokenService.revoke(laptop.rawValue());

        assertThatThrownBy(() -> refreshTokenService.rotate(laptop.rawValue(), "laptop"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("session has ended");

        // a stale client must not be mistaken for an attacker and take other sessions down with it
        assertThat(refreshTokenRepository.findById(phone.token().getId()).orElseThrow().isRevoked())
                .isFalse();
    }

    @Test
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> refreshTokenService.rotate("not-a-real-token", null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void expiredTokenIsRejected() {
        var issued = refreshTokenService.issueForNewSession(user, "JUnit");
        RefreshToken stored = refreshTokenRepository.findById(issued.token().getId()).orElseThrow();
        stored.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        refreshTokenRepository.saveAndFlush(stored);

        assertThatThrownBy(() -> refreshTokenService.rotate(issued.rawValue(), null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void disabledAccountCannotRefreshAndLosesEverySession() {
        var issued = refreshTokenService.issueForNewSession(user, "JUnit");
        user.setEnabled(false);
        userAccountRepository.saveAndFlush(user);

        assertThatThrownBy(() -> refreshTokenService.rotate(issued.rawValue(), null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("disabled");

        assertThat(refreshTokenRepository.countByUserAccountIdAndRevokedAtIsNull(user.getId())).isZero();
    }

    @Test
    void logoutRevokesOnlyThePresentedToken() {
        var keep = refreshTokenService.issueForNewSession(user, "phone");
        var drop = refreshTokenService.issueForNewSession(user, "laptop");

        refreshTokenService.revoke(drop.rawValue());

        assertThat(refreshTokenRepository.findById(drop.token().getId()).orElseThrow().getRevocationReason())
                .isEqualTo(TokenRevocationReason.LOGOUT);
        assertThat(refreshTokenRepository.findById(keep.token().getId()).orElseThrow().isRevoked()).isFalse();
    }

    @Test
    void logoutOfUnknownTokenIsSilent() {
        refreshTokenService.revoke("never-issued");
    }

    @Test
    void logoutAllRevokesEverySessionForTheOwner() {
        refreshTokenService.issueForNewSession(user, "phone");
        var laptop = refreshTokenService.issueForNewSession(user, "laptop");
        refreshTokenService.issueForNewSession(user, "tablet");

        int revoked = refreshTokenService.revokeAllSessions(laptop.rawValue());

        assertThat(revoked).isEqualTo(3);
        assertThat(refreshTokenRepository.countByUserAccountIdAndRevokedAtIsNull(user.getId())).isZero();
    }
}
