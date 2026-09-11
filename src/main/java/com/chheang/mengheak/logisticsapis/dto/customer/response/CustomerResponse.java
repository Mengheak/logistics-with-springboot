package com.chheang.mengheak.logisticsapis.dto.customer.response;

import com.chheang.mengheak.logisticsapis.dto.common.response.AddressResponse;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID id;
    private String customerCode;
    private String name;
    private String email;
    private String phone;
    private AddressResponse billingAddress;
    private UUID userId;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
