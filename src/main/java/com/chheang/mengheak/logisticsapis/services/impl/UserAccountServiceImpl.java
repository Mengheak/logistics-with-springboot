package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.common.enums.TokenRevocationReason;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.user.request.CreateUserAccountRequest;
import com.chheang.mengheak.logisticsapis.dto.user.request.UpdateUserAccountRequest;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.exception.BadRequestException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.UserAccountMapper;
import com.chheang.mengheak.logisticsapis.repositories.RefreshTokenRepository;
import com.chheang.mengheak.logisticsapis.repositories.RoleRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import com.chheang.mengheak.logisticsapis.services.UserAccountService;
import com.chheang.mengheak.logisticsapis.services.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationConfig applicationConfig;

    @Override
    public UserAccountResponse create(CreateUserAccountRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userAccountRepository.existsByEmail(email)) {
            throw ConflictException.duplicate("User account", "email", email);
        }

        UserAccount user = UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(resolveRoles(request.getRoleIds()))
                .build();

        return userAccountMapper.toResponse(userAccountRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<UserAccountResponse> list(Pageable pageable) {
        Page<UserAccountResponse> page = userAccountRepository.findAll(pageable)
                .map(userAccountMapper::toResponse);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("user"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccountResponse getOne(UUID id) {
        return userAccountMapper.toResponse(findOrThrow(id));
    }

    @Override
    public UserAccountResponse update(UUID id, UpdateUserAccountRequest request) {
        UserAccount user = findOrThrow(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        // A new password has to invalidate the old sessions, otherwise anyone holding a stolen
        // refresh token keeps their access right through the password change.
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            refreshTokenService.revokeAllSessionsForUser(user, TokenRevocationReason.CREDENTIALS_CHANGED);
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
            if (!request.getEnabled()) {
                refreshTokenService.revokeAllSessionsForUser(user, TokenRevocationReason.ACCOUNT_DISABLED);
            }
        }
        if (request.getRoleIds() != null) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }

        return userAccountMapper.toResponse(userAccountRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        UserAccount user = findOrThrow(id);
        refreshTokenRepository.deleteAllForUser(id);
        userAccountRepository.delete(user);
    }

    private UserAccount findOrThrow(UUID id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User account", id));
    }

    /** Rejects the whole request if any role id is unknown, rather than silently dropping it. */
    private Set<Role> resolveRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("at least one role id is required");
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BadRequestException("one or more role ids do not exist");
        }
        return new HashSet<>(roles);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
