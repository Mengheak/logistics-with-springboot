package com.chheang.mengheak.logisticsapis.dto.warehouse.request;

import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWarehouseRequest {

    @Size(max = 160, message = "name must be at most 160 characters")
    private String name;

    @Valid
    private AddressRequest address;

    @Positive(message = "capacity must be greater than zero")
    private Double capacityM3;

    private Boolean active;
}
