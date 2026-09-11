package com.chheang.mengheak.logisticsapis.dto.trip.response;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/** List view - no manifest, so listing trips stays a single query per page. */
@Data
public class TripSummaryResponse {
    private UUID id;
    private String tripCode;
    private String vehiclePlateNumber;
    private String driverName;
    private String originWarehouseName;
    private String destinationWarehouseName;
    private TripStatus status;
    private Instant scheduledDeparture;
    private Instant actualDeparture;
}
