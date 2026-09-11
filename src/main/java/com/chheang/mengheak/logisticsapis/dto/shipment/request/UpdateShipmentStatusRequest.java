package com.chheang.mengheak.logisticsapis.dto.shipment.request;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/** Records a scan: moves the shipment to {@code status} and appends a tracking event. */
@Data
public class UpdateShipmentStatusRequest {

    @NotNull(message = "status is required")
    private ShipmentStatus status;

    @Size(max = 300, message = "description must be at most 300 characters")
    private String description;

    private UUID locationWarehouseId;

    @Size(max = 160, message = "location note must be at most 160 characters")
    private String locationNote;

    /** Defaults to now when omitted. */
    private Instant occurredAt;
}
