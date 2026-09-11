package com.chheang.mengheak.logisticsapis.dto.trip.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateTripRequest {

    @NotNull(message = "vehicle id is required")
    private UUID vehicleId;

    @NotNull(message = "driver id is required")
    private UUID driverId;

    @NotNull(message = "origin warehouse id is required")
    private UUID originWarehouseId;

    @NotNull(message = "destination warehouse id is required")
    private UUID destinationWarehouseId;

    @NotNull(message = "scheduled departure is required")
    private Instant scheduledDeparture;

    private Instant scheduledArrival;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;
}
