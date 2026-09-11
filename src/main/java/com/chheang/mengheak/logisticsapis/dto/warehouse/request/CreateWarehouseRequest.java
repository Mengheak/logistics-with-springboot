package com.chheang.mengheak.logisticsapis.dto.warehouse.request;

import com.chheang.mengheak.logisticsapis.dto.common.request.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWarehouseRequest {

    @NotBlank(message = "warehouse code is required")
    @Size(max = 30, message = "warehouse code must be at most 30 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "warehouse code may only contain letters, digits and dashes")
    private String warehouseCode;

    @NotBlank(message = "name is required")
    @Size(max = 160, message = "name must be at most 160 characters")
    private String name;

    @NotNull(message = "address is required")
    @Valid
    private AddressRequest address;

    @Positive(message = "capacity must be greater than zero")
    private Double capacityM3;
}
