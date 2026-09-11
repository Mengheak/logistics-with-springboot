package com.chheang.mengheak.logisticsapis.dto.shipment.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ShipmentItemResponse {
    private UUID id;
    private String description;
    private int quantity;
    private BigDecimal weightKg;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private boolean fragile;
    private BigDecimal grossWeightKg;
}
