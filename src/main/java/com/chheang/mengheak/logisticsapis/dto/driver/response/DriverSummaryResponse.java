package com.chheang.mengheak.logisticsapis.dto.driver.response;

import lombok.Data;

import java.util.UUID;

@Data
public class DriverSummaryResponse {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private String phone;
}
