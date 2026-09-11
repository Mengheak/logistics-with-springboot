package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.driver.request.CreateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.request.UpdateDriverRequest;
import com.chheang.mengheak.logisticsapis.services.DriverService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created driver successfully", driverService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "status", required = false) DriverStatus status,
            @RequestParam(value = "warehouse_id", required = false) UUID warehouseId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved drivers successfully",
                driverService.list(status, warehouseId, keyword, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved driver successfully", driverService.getOne(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(Response.ok("updated driver successfully",
                driverService.update(id, request)));
    }
}
