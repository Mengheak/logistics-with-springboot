package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.CreateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.UpdateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WarehouseService {

    WarehouseResponse create(CreateWarehouseRequest request);

    PaginatedResponse<WarehouseResponse> list(String keyword, Boolean active, Pageable pageable);

    WarehouseResponse getOne(UUID id);

    WarehouseResponse update(UUID id, UpdateWarehouseRequest request);

    void deactivate(UUID id);
}
