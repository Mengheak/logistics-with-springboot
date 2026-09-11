package com.chheang.mengheak.logisticsapis.dto.shipment.response;

import com.chheang.mengheak.logisticsapis.common.enums.ServiceLevel;
import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** List/manifest view - no addresses or parcel breakdown. */
@Data
public class ShipmentSummaryResponse {
    private UUID id;
    private String trackingNumber;
    private String customerName;
    private String originWarehouseName;
    private String destinationWarehouseName;
    private ServiceLevel serviceLevel;
    private ShipmentStatus status;
    private BigDecimal totalWeightKg;
    private LocalDate expectedDeliveryDate;
}
