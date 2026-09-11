package com.chheang.mengheak.logisticsapis.dto.vehicle.response;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VehicleSummaryResponse {
    private UUID id;
    private String plateNumber;
    private VehicleType type;
    private BigDecimal capacityKg;
}
