package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.dto.role.request.CreateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.request.UpdateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.response.RoleResponse;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.RoleMapper;
import com.chheang.mengheak.logisticsapis.repositories.RoleRepository;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import com.chheang.mengheak.logisticsapis.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleMapper roleMapper;


    @Override
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw ConflictException.duplicate("Role", "name", request.getName());
        }
        return roleMapper.toResponse(roleRepository.save(roleMapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listAll() {
        return roleMapper.toResponses(roleRepository.findAll(Sort.by("name")));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getOne(UUID id) {
        return roleMapper.toResponse(findOrThrow(id));
    }

    @Override
    public RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role role = findOrThrow(id);
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    public void delete(UUID id) {
        Role role = findOrThrow(id);
        boolean assigned = userAccountRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().contains(role));
        if (assigned) {
            throw new BusinessRuleException(
                    "Role %s is still assigned to at least one account".formatted(role.getName()));
        }
        roleRepository.delete(role);
    }

    private Role findOrThrow(UUID id) {
        return roleRepository.findById(id).orElseThrow(() -> NotFoundException.of("Role", id));
    }
}
