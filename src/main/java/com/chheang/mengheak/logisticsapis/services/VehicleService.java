package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.CreateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.UpdateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.response.VehicleResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse create(CreateVehicleRequest request);

    PaginatedResponse<VehicleResponse> list(VehicleStatus status, VehicleType type, UUID warehouseId, Pageable pageable);

    VehicleResponse getOne(UUID id);

    VehicleResponse update(UUID id, UpdateVehicleRequest request);

    void retire(UUID id);
}
