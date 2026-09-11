package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.driver.request.CreateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.request.UpdateDriverRequest;
import com.chheang.mengheak.logisticsapis.dto.driver.response.DriverResponse;
import com.chheang.mengheak.logisticsapis.dto.driver.response.DriverSummaryResponse;
import com.chheang.mengheak.logisticsapis.entities.Driver;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = WarehouseMapper.class)
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "baseWarehouse", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    @Mapping(target = "status", constant = "AVAILABLE")
    Driver toEntity(CreateDriverRequest request);

    @Mapping(target = "userId", source = "userAccount.id")
    DriverResponse toResponse(Driver driver);

    DriverSummaryResponse toSummary(Driver driver);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "baseWarehouse", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    void update(@MappingTarget Driver driver, UpdateDriverRequest request);
}
