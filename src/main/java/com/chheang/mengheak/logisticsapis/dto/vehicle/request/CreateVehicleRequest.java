package com.chheang.mengheak.logisticsapis.dto.vehicle.request;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateVehicleRequest {

    @NotBlank(message = "plate number is required")
    @Size(max = 20, message = "plate number must be at most 20 characters")
    private String plateNumber;

    @NotNull(message = "vehicle type is required")
    private VehicleType type;

    @NotNull(message = "capacity in kg is required")
    @DecimalMin(value = "0.01", message = "capacity in kg must be greater than zero")
    private BigDecimal capacityKg;

    @DecimalMin(value = "0.01", message = "capacity in m3 must be greater than zero")
    private BigDecimal capacityM3;

    private LocalDate registrationExpiry;

    private UUID homeWarehouseId;
}
