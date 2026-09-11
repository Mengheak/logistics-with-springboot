package com.chheang.mengheak.logisticsapis.dto.vehicle.request;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateVehicleRequest {

    private VehicleType type;

    private VehicleStatus status;

    @DecimalMin(value = "0.01", message = "capacity in kg must be greater than zero")
    private BigDecimal capacityKg;

    @DecimalMin(value = "0.01", message = "capacity in m3 must be greater than zero")
    private BigDecimal capacityM3;

    private LocalDate registrationExpiry;

    private UUID homeWarehouseId;
}
