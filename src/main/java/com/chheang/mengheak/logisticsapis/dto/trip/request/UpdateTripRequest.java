package com.chheang.mengheak.logisticsapis.dto.trip.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/** Partial update, only allowed while the trip is still PLANNED. */
@Data
public class UpdateTripRequest {

    private UUID vehicleId;

    private UUID driverId;

    private UUID destinationWarehouseId;

    private Instant scheduledDeparture;

    private Instant scheduledArrival;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;
}
