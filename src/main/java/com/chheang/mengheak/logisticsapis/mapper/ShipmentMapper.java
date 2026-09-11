package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.shipment.request.CreateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.ShipmentItemRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentItemResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentSummaryResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.TrackingEventResponse;
import com.chheang.mengheak.logisticsapis.entities.shipment.Shipment;
import com.chheang.mengheak.logisticsapis.entities.shipment.ShipmentItem;
import com.chheang.mengheak.logisticsapis.entities.shipment.TrackingEvent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, WarehouseMapper.class})
public interface ShipmentMapper {

    /**
     * Associations, the tracking number and the derived weight are all set by the service,
     * so they are deliberately left out of the mapping.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "originWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "totalWeightKg", ignore = true)
    @Mapping(target = "expectedDeliveryDate", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "trackingEvents", ignore = true)
    Shipment toEntity(CreateShipmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shipment", ignore = true)
    ShipmentItem toItemEntity(ShipmentItemRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    ShipmentResponse toResponse(Shipment shipment);

    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "originWarehouseName", source = "originWarehouse.name")
    @Mapping(target = "destinationWarehouseName", source = "destinationWarehouse.name")
    ShipmentSummaryResponse toSummary(Shipment shipment);

    @Mapping(target = "grossWeightKg", expression = "java(item.grossWeightKg())")
    ShipmentItemResponse toItemResponse(ShipmentItem item);

    @Mapping(target = "locationWarehouseId", source = "locationWarehouse.id")
    @Mapping(target = "locationWarehouseName", source = "locationWarehouse.name")
    TrackingEventResponse toTrackingEventResponse(TrackingEvent event);

    List<TrackingEventResponse> toTrackingEventResponses(List<TrackingEvent> events);

    /** Destination hub and the parcel list are re-resolved by the service. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "originWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalWeightKg", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "trackingEvents", ignore = true)
    void update(@MappingTarget Shipment shipment, UpdateShipmentRequest request);
}
