package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.CreateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.UpdateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.response.VehicleResponse;
import com.chheang.mengheak.logisticsapis.entities.Vehicle;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.VehicleMapper;
import com.chheang.mengheak.logisticsapis.repositories.TripRepository;
import com.chheang.mengheak.logisticsapis.repositories.VehicleRepository;
import com.chheang.mengheak.logisticsapis.repositories.WarehouseRepository;
import com.chheang.mengheak.logisticsapis.services.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private static final Set<TripStatus> OPEN_TRIP_STATUSES = TripStatus.openStatuses();

    private final VehicleRepository vehicleRepository;
    private final WarehouseRepository warehouseRepository;
    private final TripRepository tripRepository;
    private final VehicleMapper vehicleMapper;
    private final ApplicationConfig applicationConfig;

    @Override
    public VehicleResponse create(CreateVehicleRequest request) {
        String plate = request.getPlateNumber().trim().toUpperCase();
        if (vehicleRepository.existsByPlateNumber(plate)) {
            throw ConflictException.duplicate("Vehicle", "plate number", plate);
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setPlateNumber(plate);
        if (request.getHomeWarehouseId() != null) {
            vehicle.setHomeWarehouse(resolveWarehouse(request.getHomeWarehouseId()));
        }

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<VehicleResponse> list(VehicleStatus status,
                                                   VehicleType type,
                                                   UUID warehouseId,
                                                   Pageable pageable) {
        Page<VehicleResponse> page = vehicleRepository.search(status, type, warehouseId, pageable)
                .map(vehicleMapper::toResponse);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("vehicle"));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getOne(UUID id) {
        return vehicleMapper.toResponse(findOrThrow(id));
    }

    @Override
    public VehicleResponse update(UUID id, UpdateVehicleRequest request) {
        Vehicle vehicle = findOrThrow(id);

        // taking a vehicle out of service mid-trip would leave the trip without transport
        if (request.getStatus() != null
                && request.getStatus() != VehicleStatus.ON_TRIP
                && vehicle.getStatus() == VehicleStatus.ON_TRIP) {
            throw new BusinessRuleException(
                    "Vehicle %s is on a trip, complete or cancel the trip first".formatted(vehicle.getPlateNumber()));
        }

        vehicleMapper.update(vehicle, request);
        if (request.getHomeWarehouseId() != null) {
            vehicle.setHomeWarehouse(resolveWarehouse(request.getHomeWarehouseId()));
        }

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void retire(UUID id) {
        Vehicle vehicle = findOrThrow(id);
        if (tripRepository.existsByVehicleIdAndStatusIn(id, OPEN_TRIP_STATUSES)) {
            throw new BusinessRuleException(
                    "Vehicle %s is committed to an open trip and cannot be retired".formatted(vehicle.getPlateNumber()));
        }
        vehicle.setStatus(VehicleStatus.RETIRED);
        vehicleRepository.save(vehicle);
    }

    private Vehicle findOrThrow(UUID id) {
        return vehicleRepository.findById(id).orElseThrow(() -> NotFoundException.of("Vehicle", id));
    }

    private Warehouse resolveWarehouse(UUID warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> NotFoundException.of("Warehouse", warehouseId));
    }
}
