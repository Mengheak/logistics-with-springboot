package com.chheang.mengheak.logisticsapis.dto.customer.request;

import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCustomerRequest {

    @Size(max = 160, message = "name must be at most 160 characters")
    private String name;

    @Email(message = "email must be valid")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @Valid
    private AddressRequest billingAddress;

    private UUID userId;

    private Boolean active;
}
