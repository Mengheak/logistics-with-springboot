package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.CreateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.UpdateVehicleRequest;
import com.chheang.mengheak.logisticsapis.services.VehicleService;
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
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created vehicle successfully", vehicleService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "status", required = false) VehicleStatus status,
            @RequestParam(value = "type", required = false) VehicleType type,
            @RequestParam(value = "warehouse_id", required = false) UUID warehouseId,
            @PageableDefault(size = 10, sort = "plateNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved vehicles successfully",
                vehicleService.list(status, type, warehouseId, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved vehicle successfully", vehicleService.getOne(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(Response.ok("updated vehicle successfully",
                vehicleService.update(id, request)));
    }

    /** Marks the vehicle RETIRED instead of deleting it, so past trips still resolve. */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> retire(@PathVariable("id") UUID id) {
        vehicleService.retire(id);
        return ResponseEntity.ok(Response.success("200", "success", "retired vehicle successfully"));
    }
}
