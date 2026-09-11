package com.chheang.mengheak.logisticsapis.dto.shipment.request;

import com.chheang.mengheak.logisticsapis.common.enums.ServiceLevel;
import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateShipmentRequest {

    @NotNull(message = "customer id is required")
    private UUID customerId;

    @NotNull(message = "origin warehouse id is required")
    private UUID originWarehouseId;

    @NotNull(message = "destination warehouse id is required")
    private UUID destinationWarehouseId;

    @NotNull(message = "pickup address is required")
    @Valid
    private AddressRequest pickupAddress;

    @NotNull(message = "delivery address is required")
    @Valid
    private AddressRequest deliveryAddress;

    @NotNull(message = "service level is required")
    private ServiceLevel serviceLevel;

    @DecimalMin(value = "0.00", message = "declared value cannot be negative")
    private BigDecimal declaredValue;

    @DecimalMin(value = "0.00", message = "cash on delivery amount cannot be negative")
    private BigDecimal codAmount;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;

    @NotEmpty(message = "a shipment must contain at least one item")
    @Valid
    private List<ShipmentItemRequest> items;
}
