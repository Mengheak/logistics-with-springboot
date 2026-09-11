package com.chheang.mengheak.logisticsapis.dto.customer.request;

import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCustomerRequest {

    @NotBlank(message = "customer code is required")
    @Size(max = 30, message = "customer code must be at most 30 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "customer code may only contain letters, digits and dashes")
    private String customerCode;

    @NotBlank(message = "name is required")
    @Size(max = 160, message = "name must be at most 160 characters")
    private String name;

    @Email(message = "email must be valid")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @Valid
    private AddressRequest billingAddress;

    /** Optional link to an existing login so the customer can use the self-service portal. */
    private UUID userId;
}
