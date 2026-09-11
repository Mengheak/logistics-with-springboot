package com.chheang.mengheak.logisticsapis.dto.shipment.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipmentItemRequest {

    @NotBlank(message = "item description is required")
    @Size(max = 200, message = "item description must be at most 200 characters")
    private String description;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity = 1;

    @NotNull(message = "item weight is required")
    @DecimalMin(value = "0.001", message = "item weight must be greater than zero")
    private BigDecimal weightKg;

    @DecimalMin(value = "0.01", message = "length must be greater than zero")
    private BigDecimal lengthCm;

    @DecimalMin(value = "0.01", message = "width must be greater than zero")
    private BigDecimal widthCm;

    @DecimalMin(value = "0.01", message = "height must be greater than zero")
    private BigDecimal heightCm;

    private boolean fragile;
}
