package com.chheang.mengheak.logisticsapis.dto.trip.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/** Loads shipments onto a trip manifest; stop order follows the order of this list. */
@Data
public class AssignShipmentsRequest {

    @NotEmpty(message = "at least one shipment id is required")
    private List<UUID> shipmentIds;
}
