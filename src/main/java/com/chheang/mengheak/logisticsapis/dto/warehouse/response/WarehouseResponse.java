package com.chheang.mengheak.logisticsapis.dto.warehouse.response;

import com.chheang.mengheak.logisticsapis.dto.common.response.AddressResponse;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WarehouseResponse {
    private UUID id;
    private String warehouseCode;
    private String name;
    private AddressResponse address;
    private Double capacityM3;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
