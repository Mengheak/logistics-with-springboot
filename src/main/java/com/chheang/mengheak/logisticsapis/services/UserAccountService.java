package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.user.request.CreateUserAccountRequest;
import com.chheang.mengheak.logisticsapis.dto.user.request.UpdateUserAccountRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserAccountService {

    UserAccountResponse create(CreateUserAccountRequest request);

    PaginatedResponse<UserAccountResponse> list(Pageable pageable);

    UserAccountResponse getOne(UUID id);

    UserAccountResponse update(UUID id, UpdateUserAccountRequest request);

    void delete(UUID id);
}
