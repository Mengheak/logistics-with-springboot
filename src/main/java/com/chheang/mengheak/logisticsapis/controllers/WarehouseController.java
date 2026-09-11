package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.CreateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.UpdateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.services.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created warehouse successfully", warehouseService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) Boolean active,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved warehouses successfully",
                warehouseService.list(keyword, active, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved warehouse successfully", warehouseService.getOne(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(Response.ok("updated warehouse successfully",
                warehouseService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deactivate(@PathVariable("id") UUID id) {
        warehouseService.deactivate(id);
        return ResponseEntity.ok(Response.success("200", "success", "deactivated warehouse successfully"));
    }
}
