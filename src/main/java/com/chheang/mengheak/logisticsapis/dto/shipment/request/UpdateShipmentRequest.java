package com.chheang.mengheak.logisticsapis.dto.shipment.request;

import com.chheang.mengheak.logisticsapis.common.enums.ServiceLevel;
import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Partial update, only allowed while the shipment has not left the origin hub.
 * A non-null {@code items} list replaces the whole parcel list.
 */
@Data
public class UpdateShipmentRequest {

    private UUID destinationWarehouseId;

    @Valid
    private AddressRequest pickupAddress;

    @Valid
    private AddressRequest deliveryAddress;

    private ServiceLevel serviceLevel;

    @DecimalMin(value = "0.00", message = "declared value cannot be negative")
    private BigDecimal declaredValue;

    @DecimalMin(value = "0.00", message = "cash on delivery amount cannot be negative")
    private BigDecimal codAmount;

    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;

    @Valid
    @Size(min = 1, message = "a shipment must contain at least one item")
    private List<ShipmentItemRequest> items;
}
