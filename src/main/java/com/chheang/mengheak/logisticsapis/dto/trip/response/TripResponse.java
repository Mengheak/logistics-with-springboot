package com.chheang.mengheak.logisticsapis.dto.trip.response;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.dto.driver.response.DriverSummaryResponse;
import com.chheang.mengheak.logisticsapis.dto.vehicle.response.VehicleSummaryResponse;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseSummaryResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class TripResponse {
    private UUID id;
    private String tripCode;
    private VehicleSummaryResponse vehicle;
    private DriverSummaryResponse driver;
    private WarehouseSummaryResponse originWarehouse;
    private WarehouseSummaryResponse destinationWarehouse;
    private TripStatus status;
    private Instant scheduledDeparture;
    private Instant scheduledArrival;
    private Instant actualDeparture;
    private Instant actualArrival;
    private String notes;
    private int shipmentCount;
    private BigDecimal manifestWeightKg;
    private List<TripManifestEntryResponse> manifest;
    private Instant createdAt;
    private Instant updatedAt;
}
