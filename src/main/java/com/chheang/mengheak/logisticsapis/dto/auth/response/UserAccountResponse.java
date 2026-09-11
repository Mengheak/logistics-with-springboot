package com.chheang.mengheak.logisticsapis.dto.auth.response;

import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class UserAccountResponse {
    private UUID id;
    private String email;
    private String fullName;
    private boolean enabled;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
