package com.chheang.mengheak.logisticsapis.dto.vehicle.response;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseSummaryResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VehicleResponse {
    private UUID id;
    private String plateNumber;
    private VehicleType type;
    private VehicleStatus status;
    private BigDecimal capacityKg;
    private BigDecimal capacityM3;
    private LocalDate registrationExpiry;
    private WarehouseSummaryResponse homeWarehouse;
    private Instant createdAt;
    private Instant updatedAt;
}
