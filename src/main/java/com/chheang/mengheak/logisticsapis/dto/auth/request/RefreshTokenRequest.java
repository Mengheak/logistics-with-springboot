package com.chheang.mengheak.logisticsapis.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Carries the refresh token in the body rather than the URL, so it stays out of access logs. */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refresh token is required")
    @Size(max = 200, message = "refresh token is not a valid length")
    private String refreshToken;
}
