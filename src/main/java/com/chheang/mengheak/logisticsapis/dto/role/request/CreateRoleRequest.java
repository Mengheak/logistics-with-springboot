package com.chheang.mengheak.logisticsapis.dto.role.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "role name is required")
    @Size(max = 30, message = "role name must be at most 30 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$",
            message = "role name must be upper snake case and must not include the ROLE_ prefix")
    private String name;

    @Size(max = 160, message = "description must be at most 160 characters")
    private String description;
}
