package com.chheang.mengheak.logisticsapis.dto.shipment.response;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TrackingEventResponse {
    private UUID id;
    private ShipmentStatus status;
    private String description;
    private UUID locationWarehouseId;
    private String locationWarehouseName;
    private String locationNote;
    private Instant occurredAt;
    private String recordedBy;
}
