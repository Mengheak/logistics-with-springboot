package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.dto.trip.request.AssignShipmentsRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.CreateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.UpdateTripRequest;
import com.chheang.mengheak.logisticsapis.services.TripService;
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
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody CreateTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("created trip successfully", tripService.create(request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @GetMapping
    public ResponseEntity<Response> list(
            @RequestParam(value = "status", required = false) TripStatus status,
            @RequestParam(value = "vehicle_id", required = false) UUID vehicleId,
            @RequestParam(value = "driver_id", required = false) UUID driverId,
            @RequestParam(value = "origin_warehouse_id", required = false) UUID originWarehouseId,
            @PageableDefault(size = 10, sort = "scheduledDeparture", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(Response.ok("retrieved trips successfully",
                tripService.list(status, vehicleId, driverId, originWarehouseId, pageable)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @GetMapping("/{id}")
    public ResponseEntity<Response> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("retrieved trip successfully", tripService.getOne(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable("id") UUID id,
                                           @Valid @RequestBody UpdateTripRequest request) {
        return ResponseEntity.ok(Response.ok("updated trip successfully", tripService.update(id, request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @PostMapping("/{id}/shipments")
    public ResponseEntity<Response> assignShipments(@PathVariable("id") UUID id,
                                                    @Valid @RequestBody AssignShipmentsRequest request) {
        return ResponseEntity.ok(Response.ok("loaded shipments onto trip successfully",
                tripService.assignShipments(id, request)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @DeleteMapping("/{id}/shipments/{shipmentId}")
    public ResponseEntity<Response> removeShipment(@PathVariable("id") UUID id,
                                                   @PathVariable("shipmentId") UUID shipmentId) {
        return ResponseEntity.ok(Response.ok("removed shipment from trip successfully",
                tripService.removeShipment(id, shipmentId)));
    }

    /** Departure scan: locks the manifest and moves every loaded shipment to IN_TRANSIT. */
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<Response> dispatch(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("dispatched trip successfully", tripService.dispatch(id)));
    }

    /** Arrival scan: frees the vehicle and driver and checks the shipments into the hub. */
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<Response> complete(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("completed trip successfully", tripService.complete(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> cancel(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(Response.ok("cancelled trip successfully", tripService.cancel(id)));
    }
}
