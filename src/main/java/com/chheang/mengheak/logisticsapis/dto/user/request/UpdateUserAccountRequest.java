package com.chheang.mengheak.logisticsapis.dto.user.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/** Partial update - every field is optional and only non-null values are applied. */
@Data
public class UpdateUserAccountRequest {

    @Size(max = 120, message = "full name must be at most 120 characters")
    private String fullName;

    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    private Boolean enabled;

    /** When present, replaces the whole role set. */
    private Set<UUID> roleIds;
}
