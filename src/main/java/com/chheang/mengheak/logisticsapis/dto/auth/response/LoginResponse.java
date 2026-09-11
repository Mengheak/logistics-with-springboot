package com.chheang.mengheak.logisticsapis.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * The token pair, returned by both login and refresh. {@code refreshToken} is the only time the
 * raw refresh value is ever visible - the server keeps just its hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private String refreshToken;
    private long refreshExpiresInSeconds;
    private String email;
    private String fullName;
    private Set<String> roles;
}
