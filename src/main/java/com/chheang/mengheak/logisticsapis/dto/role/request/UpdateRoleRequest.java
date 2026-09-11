package com.chheang.mengheak.logisticsapis.dto.role.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @Size(max = 160, message = "description must be at most 160 characters")
    private String description;
}
