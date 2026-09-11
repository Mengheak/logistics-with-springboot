package com.chheang.mengheak.logisticsapis.services.auth;

import com.chheang.mengheak.logisticsapis.dto.auth.request.LoginRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RefreshTokenRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RegisterRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.response.LoginResponse;
import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;

public interface AuthService {

    UserAccountResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String clientInfo);

    /** Exchanges a refresh token for a new access + refresh pair. */
    LoginResponse refresh(RefreshTokenRequest request, String clientInfo);

    void logout(RefreshTokenRequest request);

    /** Signs the token's owner out of every device. Returns how many sessions were ended. */
    int logoutAll(RefreshTokenRequest request);

    UserAccountResponse currentUser();
}
