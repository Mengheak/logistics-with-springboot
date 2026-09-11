package com.chheang.mengheak.logisticsapis.dto.warehouse.response;

import lombok.Data;

import java.util.UUID;

/** Trimmed warehouse view embedded inside shipment and trip payloads. */
@Data
public class WarehouseSummaryResponse {
    private UUID id;
    private String warehouseCode;
    private String name;
    private String city;
}
