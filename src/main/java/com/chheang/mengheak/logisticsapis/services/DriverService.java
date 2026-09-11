package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.driver.request.CreateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.request.UpdateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.response.DriverResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DriverService {

    DriverResponse create(CreateDriverRequest request);

    PaginatedResponse<DriverResponse> list(DriverStatus status, UUID warehouseId, String keyword, Pageable pageable);

    DriverResponse getOne(UUID id);

    DriverResponse update(UUID id, UpdateDriverRequest request);
}
