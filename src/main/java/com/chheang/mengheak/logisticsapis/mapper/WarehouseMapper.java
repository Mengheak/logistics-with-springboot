package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.warehouse.request.CreateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.request.UpdateWarehouseRequest;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseResponse;
import com.chheang.mengheak.logisticsapis.dto.warehouse.response.WarehouseSummaryResponse;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface WarehouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Warehouse toEntity(CreateWarehouseRequest request);

    WarehouseResponse toResponse(Warehouse warehouse);

    @Mapping(target = "city", source = "address.city")
    WarehouseSummaryResponse toSummary(Warehouse warehouse);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "warehouseCode", ignore = true)
    void update(@MappingTarget Warehouse warehouse, UpdateWarehouseRequest request);
}
