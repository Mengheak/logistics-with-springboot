package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.services.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public tracking. Intentionally unauthenticated - the tracking number is the credential, and
 * the payload carries only status and scan history, never addresses or contact details.
 */
@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final ShipmentService shipmentService;

    public TrackingController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<Response> track(@PathVariable("trackingNumber") String trackingNumber) {
        return ResponseEntity.ok(Response.ok("retrieved tracking history successfully",
                shipmentService.track(trackingNumber)));
    }
}
