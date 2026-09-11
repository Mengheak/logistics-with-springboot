package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.CreateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentStatusRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentSummaryResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentTrackingResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShipmentService {

    ShipmentResponse create(CreateShipmentRequest request);

    PaginatedResponse<ShipmentSummaryResponse> list(ShipmentStatus status,
                                                    UUID customerId,
                                                    UUID originWarehouseId,
                                                    UUID destinationWarehouseId,
                                                    String keyword,
                                                    Pageable pageable);

    ShipmentResponse getOne(UUID id);

    ShipmentResponse update(UUID id, UpdateShipmentRequest request);

    ShipmentResponse updateStatus(UUID id, UpdateShipmentStatusRequest request);

    ShipmentResponse cancel(UUID id);

    /** Public lookup by tracking number - no authentication required. */
    ShipmentTrackingResponse track(String trackingNumber);
}
