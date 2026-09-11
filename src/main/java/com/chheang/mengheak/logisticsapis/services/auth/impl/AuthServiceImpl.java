package com.chheang.mengheak.logisticsapis.services.auth.impl;

import com.chheang.mengheak.logisticsapis.common.enums.RoleName;
import com.chheang.mengheak.logisticsapis.dto.auth.request.LoginRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RefreshTokenRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RegisterRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.response.LoginResponse;
import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.exception.UnauthorizedException;
import com.chheang.mengheak.logisticsapis.mapper.UserAccountMapper;
import com.chheang.mengheak.logisticsapis.models.AuthUser;
import com.chheang.mengheak.logisticsapis.repositories.RoleRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import com.chheang.mengheak.logisticsapis.security.CurrentUserProvider;
import com.chheang.mengheak.logisticsapis.services.auth.AuthService;
import com.chheang.mengheak.logisticsapis.services.auth.JwtService;
import com.chheang.mengheak.logisticsapis.services.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountMapper userAccountMapper;
    private final CurrentUserProvider currentUserProvider;


    /**
     * Self-service sign-up. The role is hard-coded to CUSTOMER on purpose: accepting a role from
     * the request body on a public endpoint would let anyone mint themselves an admin account.
     */
    @Override
    public UserAccountResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userAccountRepository.existsByEmail(email)) {
            throw ConflictException.duplicate("User account", "email", email);
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
                .orElseThrow(() -> NotFoundException.of("Role", RoleName.CUSTOMER.name()));

        UserAccount user = UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(Set.of(customerRole))
                .build();

        return userAccountMapper.toResponse(userAccountRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientInfo) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(), request.getPassword()));

        AuthUser principal = (AuthUser) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> NotFoundException.of("User account", principal.getId()));

        RefreshTokenService.IssuedRefreshToken refreshToken =
                refreshTokenService.issueForNewSession(user, clientInfo);

        return buildTokenPair(principal, refreshToken);
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request, String clientInfo) {
        RefreshTokenService.IssuedRefreshToken rotated =
                refreshTokenService.rotate(request.getRefreshToken(), clientInfo);

        // Authorities are re-read from the account, so a role change or a disabled account takes
        // effect on the next refresh instead of lingering until the old access token expires.
        AuthUser principal = new AuthUser(rotated.token().getUserAccount());

        return buildTokenPair(principal, rotated);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    @Override
    public int logoutAll(RefreshTokenRequest request) {
        return refreshTokenService.revokeAllSessions(request.getRefreshToken());
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccountResponse currentUser() {
        AuthUser authUser = currentUserProvider.current()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user in this request"));

        UserAccount user = userAccountRepository.findById(authUser.getId())
                .orElseThrow(() -> NotFoundException.of("User account", authUser.getId()));

        return userAccountMapper.toResponse(user);
    }

    private LoginResponse buildTokenPair(AuthUser principal,
                                         RefreshTokenService.IssuedRefreshToken refreshToken) {
        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getAccessTokenValiditySeconds())
                .refreshToken(refreshToken.rawValue())
                .refreshExpiresInSeconds(refreshTokenService.refreshValiditySeconds())
                .email(principal.getEmail())
                .fullName(principal.getFullName())
                .roles(principal.getRoleNames())
                .build();
    }
}
