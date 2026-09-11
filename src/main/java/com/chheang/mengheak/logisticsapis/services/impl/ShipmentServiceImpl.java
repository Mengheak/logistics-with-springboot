package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.common.util.ReferenceCodeGenerator;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.CreateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.ShipmentItemRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.request.UpdateShipmentStatusRequest;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentSummaryResponse;
import com.chheang.mengheak.logisticsapis.dto.shipment.response.ShipmentTrackingResponse;
import com.chheang.mengheak.logisticsapis.entities.Customer;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.entities.shipment.Shipment;
import com.chheang.mengheak.logisticsapis.entities.shipment.TrackingEvent;
import com.chheang.mengheak.logisticsapis.exception.BadRequestException;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.ShipmentMapper;
import com.chheang.mengheak.logisticsapis.repositories.CustomerRepository;
import com.chheang.mengheak.logisticsapis.repositories.ShipmentRepository;
import com.chheang.mengheak.logisticsapis.repositories.TripShipmentRepository;
import com.chheang.mengheak.logisticsapis.repositories.WarehouseRepository;
import com.chheang.mengheak.logisticsapis.security.CurrentUserProvider;
import com.chheang.mengheak.logisticsapis.services.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private static final Set<TripStatus> OPEN_TRIP_STATUSES = TripStatus.openStatuses();

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final TripShipmentRepository tripShipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final ReferenceCodeGenerator codeGenerator;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationConfig applicationConfig;
    @Override
    public ShipmentResponse create(CreateShipmentRequest request) {
        if (request.getOriginWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new BadRequestException("origin and destination warehouse must be different");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> NotFoundException.of("Customer", request.getCustomerId()));
        if (!customer.isActive()) {
            throw new BusinessRuleException(
                    "Customer %s is inactive and cannot book shipments".formatted(customer.getCustomerCode()));
        }

        Warehouse origin = resolveActiveWarehouse(request.getOriginWarehouseId());
        Warehouse destination = resolveActiveWarehouse(request.getDestinationWarehouseId());

        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setCustomer(customer);
        shipment.setOriginWarehouse(origin);
        shipment.setDestinationWarehouse(destination);
        shipment.setTrackingNumber(
                codeGenerator.generateTrackingNumber(shipmentRepository::existsByTrackingNumber));
        shipment.setExpectedDeliveryDate(
                LocalDate.now().plusDays(request.getServiceLevel().getTransitDays()));

        replaceItems(shipment, request.getItems());

        Instant now = Instant.now();
        shipment.addTrackingEvent(TrackingEvent.builder()
                .status(ShipmentStatus.CREATED)
                .description("Shipment booked")
                .locationWarehouse(origin)
                .recordedBy(currentUserProvider.currentEmailOrSystem())
                .occurredAt(now)
                .build());

        return shipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShipmentSummaryResponse> list(ShipmentStatus status,
                                                           UUID customerId,
                                                           UUID originWarehouseId,
                                                           UUID destinationWarehouseId,
                                                           String keyword,
                                                           Pageable pageable) {
        Page<ShipmentSummaryResponse> page = shipmentRepository
                .search(status, customerId, originWarehouseId, destinationWarehouseId,
                        keyword == null || keyword.isBlank() ? null : keyword.trim(), pageable)
                .map(shipmentMapper::toSummary);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("shipment"));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getOne(UUID id) {
        return shipmentMapper.toResponse(findOrThrow(id));
    }

    /** Editing is only safe before the parcels are collected - after that the manifest is printed. */
    @Override
    public ShipmentResponse update(UUID id, UpdateShipmentRequest request) {
        Shipment shipment = findOrThrow(id);
        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new BusinessRuleException(
                    "Shipment %s has already been collected and can no longer be edited"
                            .formatted(shipment.getTrackingNumber()));
        }

        shipmentMapper.update(shipment, request);

        if (request.getDestinationWarehouseId() != null) {
            if (request.getDestinationWarehouseId().equals(shipment.getOriginWarehouse().getId())) {
                throw new BadRequestException("origin and destination warehouse must be different");
            }
            shipment.setDestinationWarehouse(resolveActiveWarehouse(request.getDestinationWarehouseId()));
        }
        if (request.getServiceLevel() != null) {
            shipment.setExpectedDeliveryDate(
                    LocalDate.now().plusDays(request.getServiceLevel().getTransitDays()));
        }
        if (request.getItems() != null) {
            replaceItems(shipment, request.getItems());
        }

        return shipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentResponse updateStatus(UUID id, UpdateShipmentStatusRequest request) {
        Shipment shipment = findOrThrow(id);

        Warehouse location = request.getLocationWarehouseId() == null
                ? null
                : warehouseRepository.findById(request.getLocationWarehouseId())
                        .orElseThrow(() -> NotFoundException.of("Warehouse", request.getLocationWarehouseId()));

        Instant occurredAt = request.getOccurredAt() == null ? Instant.now() : request.getOccurredAt();
        if (occurredAt.isAfter(Instant.now())) {
            throw new BadRequestException("occurred at cannot be in the future");
        }

        shipment.transitionTo(
                request.getStatus(),
                request.getDescription(),
                location,
                request.getLocationNote(),
                currentUserProvider.currentEmailOrSystem(),
                occurredAt);

        return shipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentResponse cancel(UUID id) {
        Shipment shipment = findOrThrow(id);

        if (tripShipmentRepository.existsByShipmentIdAndTripStatusIn(id, OPEN_TRIP_STATUSES)) {
            throw new ConflictException(
                    "Shipment %s is on an open trip, remove it from the manifest first"
                            .formatted(shipment.getTrackingNumber()));
        }

        shipment.transitionTo(
                ShipmentStatus.CANCELLED,
                "Shipment cancelled",
                shipment.getOriginWarehouse(),
                null,
                currentUserProvider.currentEmailOrSystem(),
                Instant.now());

        return shipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingResponse track(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber.trim().toUpperCase())
                .orElseThrow(() -> NotFoundException.of("Shipment", trackingNumber));

        return ShipmentTrackingResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .originWarehouseName(shipment.getOriginWarehouse().getName())
                .destinationWarehouseName(shipment.getDestinationWarehouse().getName())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .deliveredAt(shipment.getDeliveredAt())
                .events(shipmentMapper.toTrackingEventResponses(shipment.getTrackingEvents()))
                .build();
    }

    private Shipment findOrThrow(UUID id) {
        return shipmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> NotFoundException.of("Shipment", id));
    }

    private Warehouse resolveActiveWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> NotFoundException.of("Warehouse", warehouseId));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException(
                    "Warehouse %s is inactive".formatted(warehouse.getWarehouseCode()));
        }
        return warehouse;
    }

    /** Replaces the parcel list wholesale and re-derives the total weight from it. */
    private void replaceItems(Shipment shipment, List<ShipmentItemRequest> itemRequests) {
        shipment.getItems().clear();
        itemRequests.forEach(itemRequest -> shipment.addItem(shipmentMapper.toItemEntity(itemRequest)));
        shipment.recalculateTotalWeight();
    }
}
