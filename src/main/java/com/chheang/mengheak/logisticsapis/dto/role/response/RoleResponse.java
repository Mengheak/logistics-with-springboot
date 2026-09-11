package com.chheang.mengheak.logisticsapis.dto.role.response;

import lombok.Data;

import java.util.UUID;

@Data
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
}
