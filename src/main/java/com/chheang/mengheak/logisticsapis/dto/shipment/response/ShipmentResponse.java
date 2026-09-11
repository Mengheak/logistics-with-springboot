package com.chheang.mengheak.logisticsapis.dto.shipment.response;

import com.chheang.mengheak.logisticsapis.common.enums.ServiceLevel;
import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.dto.common.response.AddressResponse;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseSummaryResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ShipmentResponse {
    private UUID id;
    private String trackingNumber;
    private UUID customerId;
    private String customerName;
    private WarehouseSummaryResponse originWarehouse;
    private WarehouseSummaryResponse destinationWarehouse;
    private AddressResponse pickupAddress;
    private AddressResponse deliveryAddress;
    private ServiceLevel serviceLevel;
    private ShipmentStatus status;
    private BigDecimal totalWeightKg;
    private BigDecimal declaredValue;
    private BigDecimal codAmount;
    private LocalDate expectedDeliveryDate;
    private Instant deliveredAt;
    private String notes;
    private List<ShipmentItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
