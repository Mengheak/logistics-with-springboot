package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.role.request.CreateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.response.RoleResponse;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    Role toEntity(CreateRoleRequest request);

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponses(List<Role> roles);
}
