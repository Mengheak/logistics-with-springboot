package com.chheang.mengheak.logisticsapis.dto.trip.response;

import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentSummaryResponse;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TripManifestEntryResponse {
    private UUID id;
    private int stopSequence;
    private Instant loadedAt;
    private Instant unloadedAt;
    private ShipmentSummaryResponse shipment;
}
