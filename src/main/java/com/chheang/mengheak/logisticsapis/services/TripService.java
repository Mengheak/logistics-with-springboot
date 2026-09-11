package com.chheang.mengheak.logisticsapis.services;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.request.AssignShipmentsRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.CreateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.UpdateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TripService {

    TripResponse create(CreateTripRequest request);

    PaginatedResponse<TripSummaryResponse> list(TripStatus status,
                                                UUID vehicleId,
                                                UUID driverId,
                                                UUID originWarehouseId,
                                                Pageable pageable);

    TripResponse getOne(UUID id);

    TripResponse update(UUID id, UpdateTripRequest request);

    TripResponse assignShipments(UUID tripId, AssignShipmentsRequest request);

    TripResponse removeShipment(UUID tripId, UUID shipmentId);

    /** Leaves the origin hub: locks the manifest and moves every shipment to IN_TRANSIT. */
    TripResponse dispatch(UUID id);

    /** Arrives at the destination hub: frees vehicle and driver, moves shipments to the hub. */
    TripResponse complete(UUID id);

    TripResponse cancel(UUID id);
}
