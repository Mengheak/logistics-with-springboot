package com.chheang.mengheak.logisticsapis.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Public self-service sign-up. Always lands on the CUSTOMER role - staff accounts are
 * created by an admin through {@code POST /api/v1/users}.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    @NotBlank(message = "full name is required")
    @Size(max = 120, message = "full name must be at most 120 characters")
    private String fullName;
}
