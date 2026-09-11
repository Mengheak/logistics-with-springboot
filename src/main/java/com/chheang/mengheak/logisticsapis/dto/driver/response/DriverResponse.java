package com.chheang.mengheak.logisticsapis.dto.driver.response;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseSummaryResponse;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DriverResponse {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private String phone;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private DriverStatus status;
    private WarehouseSummaryResponse baseWarehouse;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
}
