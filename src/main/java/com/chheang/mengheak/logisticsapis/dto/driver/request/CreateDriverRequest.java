package com.chheang.mengheak.logisticsapis.dto.driver.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateDriverRequest {

    @NotBlank(message = "employee code is required")
    @Size(max = 30, message = "employee code must be at most 30 characters")
    private String employeeCode;

    @NotBlank(message = "full name is required")
    @Size(max = 120, message = "full name must be at most 120 characters")
    private String fullName;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @NotBlank(message = "license number is required")
    @Size(max = 40, message = "license number must be at most 40 characters")
    private String licenseNumber;

    @NotNull(message = "license expiry is required")
    @Future(message = "license expiry must be in the future")
    private LocalDate licenseExpiry;

    private UUID baseWarehouseId;

    /** Optional link to an existing login so the driver can use the mobile app. */
    private UUID userId;
}
