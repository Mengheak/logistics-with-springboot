package com.chheang.mengheak.logisticsapis.dto.driver.request;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateDriverRequest {

    @Size(max = 120, message = "full name must be at most 120 characters")
    private String fullName;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @Size(max = 40, message = "license number must be at most 40 characters")
    private String licenseNumber;

    private LocalDate licenseExpiry;

    private DriverStatus status;

    private UUID baseWarehouseId;

    private UUID userId;
}
