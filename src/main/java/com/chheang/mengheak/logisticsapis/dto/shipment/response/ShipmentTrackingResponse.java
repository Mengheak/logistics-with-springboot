package com.chheang.mengheak.logisticsapis.dto.shipment.response;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** What the public tracking lookup returns: status, ETA and the scan history. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {
    private String trackingNumber;
    private ShipmentStatus status;
    private String originWarehouseName;
    private String destinationWarehouseName;
    private LocalDate expectedDeliveryDate;
    private Instant deliveredAt;
    private List<TrackingEventResponse> events;
}
