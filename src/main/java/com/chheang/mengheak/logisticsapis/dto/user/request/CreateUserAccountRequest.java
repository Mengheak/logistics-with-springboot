package com.chheang.mengheak.logisticsapis.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/** Admin-side account creation, where roles are assigned explicitly. */
@Data
public class CreateUserAccountRequest {

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    @Size(max = 120, message = "full name must be at most 120 characters")
    private String fullName;

    @NotEmpty(message = "at least one role id is required")
    private Set<UUID> roleIds;
}
