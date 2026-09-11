package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.customer.request.CreateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.request.UpdateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.response.CustomerResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CreateCustomerRequest request);

    PaginatedResponse<CustomerResponse> list(String keyword, Boolean active, Pageable pageable);

    CustomerResponse getOne(UUID id);

    CustomerResponse getByCode(String customerCode);

    CustomerResponse update(UUID id, UpdateCustomerRequest request);

    void deactivate(UUID id);
}
