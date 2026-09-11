package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.customer.request.CreateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.request.UpdateCustomerRequest;
import com.chheang.mengheak.logisticsapis.dto.customer.response.CustomerResponse;
import com.chheang.mengheak.logisticsapis.entities.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    @Mapping(target = "active", constant = "true")
    Customer toEntity(CreateCustomerRequest request);

    @Mapping(target = "userId", source = "userAccount.id")
    CustomerResponse toResponse(Customer customer);

    /**
     * Applies only the non-null fields of a PATCH body. The {@code userAccount} link is
     * resolved in the service because it needs a repository lookup.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    void update(@MappingTarget Customer customer, UpdateCustomerRequest request);
}
