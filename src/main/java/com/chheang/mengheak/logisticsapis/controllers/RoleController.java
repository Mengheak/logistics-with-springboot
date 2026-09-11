package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.role.request.CreateRoleRequest;
import com.chheang.mengheak.logisticsapis.dto.role.request.UpdateRoleRequest;
import com.chheang.mengheak.logisticsapis.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created role successfully", roleService.create(request)));
    }

    @GetMapping
    public ResponseEntity<Response> listAll() {
        return ResponseEntity.ok(Response.ok("retrieved roles successfully", roleService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved role successfully", roleService.getOne(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(Response.ok("updated role successfully", roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(@PathVariable("id") UUID id) {
        roleService.delete(id);
        return ResponseEntity.ok(Response.success("200", "success", "deleted role successfully"));
    }
}
