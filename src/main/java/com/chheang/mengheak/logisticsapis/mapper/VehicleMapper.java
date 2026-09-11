package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.vehicle.request.CreateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.request.UpdateVehicleRequest;
import com.chheang.mengheak.logisticsapis.dto.vehicle.response.VehicleResponse;
import com.chheang.mengheak.logisticsapis.dto.vehicle.response.VehicleSummaryResponse;
import com.chheang.mengheak.logisticsapis.entities.Vehicle;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = WarehouseMapper.class)
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "homeWarehouse", ignore = true)
    @Mapping(target = "status", constant = "AVAILABLE")
    Vehicle toEntity(CreateVehicleRequest request);

    VehicleResponse toResponse(Vehicle vehicle);

    VehicleSummaryResponse toSummary(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plateNumber", ignore = true)
    @Mapping(target = "homeWarehouse", ignore = true)
    void update(@MappingTarget Vehicle vehicle, UpdateVehicleRequest request);
}
