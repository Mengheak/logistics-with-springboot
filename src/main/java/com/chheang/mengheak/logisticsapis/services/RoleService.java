package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.dto.role.request.CreateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.request.UpdateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    List<RoleResponse> listAll();

    RoleResponse getOne(UUID id);

    RoleResponse update(UUID id, UpdateRoleRequest request);

    void delete(UUID id);
}
