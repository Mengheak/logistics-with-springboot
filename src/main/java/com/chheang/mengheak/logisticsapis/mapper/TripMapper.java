package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.trip.request.CreateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.UpdateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripManifestEntryResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripSummaryResponse;
import com.chheang.mengheak.logisticsapis.entities.trip.Trip;
import com.chheang.mengheak.logisticsapis.entities.trip.TripShipment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {VehicleMapper.class, DriverMapper.class, WarehouseMapper.class, ShipmentMapper.class})
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tripCode", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "originWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "actualDeparture", ignore = true)
    @Mapping(target = "actualArrival", ignore = true)
    @Mapping(target = "manifest", ignore = true)
    Trip toEntity(CreateTripRequest request);

    @Mapping(target = "shipmentCount", expression = "java(trip.getManifest().size())")
    @Mapping(target = "manifestWeightKg", expression = "java(trip.manifestWeightKg())")
    TripResponse toResponse(Trip trip);

    @Mapping(target = "vehiclePlateNumber", source = "vehicle.plateNumber")
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "originWarehouseName", source = "originWarehouse.name")
    @Mapping(target = "destinationWarehouseName", source = "destinationWarehouse.name")
    TripSummaryResponse toSummary(Trip trip);

    TripManifestEntryResponse toManifestEntryResponse(TripShipment tripShipment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tripCode", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "originWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "manifest", ignore = true)
    void update(@MappingTarget Trip trip, UpdateTripRequest request);
}
