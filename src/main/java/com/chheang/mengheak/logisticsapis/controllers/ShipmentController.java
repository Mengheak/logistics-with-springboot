package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.CreateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentStatusRequest;
import com.chheang.mengheak.logisticsapis.services.ShipmentService;
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
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created shipment successfully", shipmentService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "status", required = false) ShipmentStatus status,
            @RequestParam(value = "customer_id", required = false) UUID customerId,
            @RequestParam(value = "origin_warehouse_id", required = false) UUID originWarehouseId,
            @RequestParam(value = "destination_warehouse_id", required = false) UUID destinationWarehouseId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved shipments successfully",
                shipmentService.list(status, customerId, originWarehouseId, destinationWarehouseId,
                        keyword, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved shipment successfully", shipmentService.getOne(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(Response.ok("updated shipment successfully",
                shipmentService.update(id, request)));
    }

    /** Records a scan. Drivers use this from the field, which is why they are allowed here. */
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Response> updateStatus(@PathVariable("id") UUID id,
                                                 @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return ResponseEntity.ok(Response.ok("updated shipment status successfully",
                shipmentService.updateStatus(id, request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> cancel(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("cancelled shipment successfully", shipmentService.cancel(id)));
    }
}
