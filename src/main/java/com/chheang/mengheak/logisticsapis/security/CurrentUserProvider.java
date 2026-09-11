package com.chheang.mengheak.logisticsapis.security;

import com.chheang.mengheak.logisticsapis.models.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lets services stamp "who did this" on tracking events without every controller having to
 * thread the principal through as a parameter.
 */
@Component
public class CurrentUserProvider {

    public Optional<AuthUser> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            return Optional.empty();
        }
        return Optional.of(authUser);
    }

    /** Email of the caller, or {@code system} for unauthenticated/automatic actions. */
    public String currentEmailOrSystem() {
        return current().map(AuthUser::getEmail).orElse("system");
    }
}
