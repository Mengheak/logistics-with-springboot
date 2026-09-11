package com.chheang.mengheak.logisticsapis.services.impl;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.util.ReferenceCodeGenerator;
import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.dto.base.PaginatedResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.request.AssignShipmentsRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.CreateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.request.UpdateTripRequest;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripResponse;
import com.chheang.mengheak.logisticsapis.dto.trip.response.TripSummaryResponse;
import com.chheang.mengheak.logisticsapis.entities.Driver;
import com.chheang.mengheak.logisticsapis.entities.Vehicle;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.entities.shipment.Shipment;
import com.chheang.mengheak.logisticsapis.entities.trip.Trip;
import com.chheang.mengheak.logisticsapis.entities.trip.TripShipment;
import com.chheang.mengheak.logisticsapis.exception.BadRequestException;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import com.chheang.mengheak.logisticsapis.exception.ConflictException;
import com.chheang.mengheak.logisticsapis.exception.NotFoundException;
import com.chheang.mengheak.logisticsapis.mapper.TripMapper;
import com.chheang.mengheak.logisticsapis.repositories.DriverRepository;
import com.chheang.mengheak.logisticsapis.repositories.ShipmentRepository;
import com.chheang.mengheak.logisticsapis.repositories.TripRepository;
import com.chheang.mengheak.logisticsapis.repositories.TripShipmentRepository;
import com.chheang.mengheak.logisticsapis.repositories.VehicleRepository;
import com.chheang.mengheak.logisticsapis.repositories.WarehouseRepository;
import com.chheang.mengheak.logisticsapis.security.CurrentUserProvider;
import com.chheang.mengheak.logisticsapis.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private static final Set<TripStatus> OPEN_TRIP_STATUSES = TripStatus.openStatuses();

    private final TripRepository tripRepository;
    private final TripShipmentRepository tripShipmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final WarehouseRepository warehouseRepository;
    private final TripMapper tripMapper;
    private final ReferenceCodeGenerator codeGenerator;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationConfig applicationConfig;

    @Override
    public TripResponse create(CreateTripRequest request) {
        if (request.getOriginWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new BadRequestException("origin and destination warehouse must be different");
        }
        if (request.getScheduledArrival() != null
                && !request.getScheduledArrival().isAfter(request.getScheduledDeparture())) {
            throw new BadRequestException("scheduled arrival must be after scheduled departure");
        }

        Vehicle vehicle = resolveAvailableVehicle(request.getVehicleId());
        Driver driver = resolveAvailableDriver(request.getDriverId());

        Trip trip = tripMapper.toEntity(request);
        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setOriginWarehouse(resolveWarehouse(request.getOriginWarehouseId()));
        trip.setDestinationWarehouse(resolveWarehouse(request.getDestinationWarehouseId()));
        trip.setTripCode(codeGenerator.generateTripCode(tripRepository::existsByTripCode));

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TripSummaryResponse> list(TripStatus status,
                                                       UUID vehicleId,
                                                       UUID driverId,
                                                       UUID originWarehouseId,
                                                       Pageable pageable) {
        Page<TripSummaryResponse> page = tripRepository
                .search(status, vehicleId, driverId, originWarehouseId, pageable)
                .map(tripMapper::toSummary);
        return PaginatedResponse.from(page, applicationConfig.getPagination().getUrlByResource("trip"));
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getOne(UUID id) {
        return tripMapper.toResponse(findOrThrow(id));
    }

    @Override
    public TripResponse update(UUID id, UpdateTripRequest request) {
        Trip trip = findOrThrow(id);
        requirePlanned(trip, "updated");

        tripMapper.update(trip, request);

        if (request.getVehicleId() != null && !request.getVehicleId().equals(trip.getVehicle().getId())) {
            trip.setVehicle(resolveAvailableVehicle(request.getVehicleId()));
        }
        if (request.getDriverId() != null && !request.getDriverId().equals(trip.getDriver().getId())) {
            trip.setDriver(resolveAvailableDriver(request.getDriverId()));
        }
        if (request.getDestinationWarehouseId() != null) {
            if (request.getDestinationWarehouseId().equals(trip.getOriginWarehouse().getId())) {
                throw new BadRequestException("origin and destination warehouse must be different");
            }
            trip.setDestinationWarehouse(resolveWarehouse(request.getDestinationWarehouseId()));
        }
        if (trip.getScheduledArrival() != null
                && !trip.getScheduledArrival().isAfter(trip.getScheduledDeparture())) {
            throw new BadRequestException("scheduled arrival must be after scheduled departure");
        }

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse assignShipments(UUID tripId, AssignShipmentsRequest request) {
        Trip trip = findOrThrow(tripId);
        requirePlanned(trip, "loaded");

        int nextSequence = tripShipmentRepository.findMaxStopSequence(tripId);
        BigDecimal loadedWeight = trip.manifestWeightKg();
        BigDecimal capacity = trip.getVehicle().getCapacityKg();

        for (UUID shipmentId : request.getShipmentIds()) {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));

            if (!shipment.getStatus().isAssignable()) {
                throw new BusinessRuleException(
                        "Shipment %s is %s and cannot be loaded onto a trip"
                                .formatted(shipment.getTrackingNumber(), shipment.getStatus()));
            }
            if (tripShipmentRepository.findByTripIdAndShipmentId(tripId, shipmentId).isPresent()) {
                throw new ConflictException("Shipment %s is already on this trip"
                        .formatted(shipment.getTrackingNumber()));
            }
            if (tripShipmentRepository.existsByShipmentIdAndTripStatusIn(shipmentId, OPEN_TRIP_STATUSES)) {
                throw new ConflictException("Shipment %s is already committed to another open trip"
                        .formatted(shipment.getTrackingNumber()));
            }

            loadedWeight = loadedWeight.add(shipment.getTotalWeightKg());
            if (loadedWeight.compareTo(capacity) > 0) {
                throw new BusinessRuleException(
                        "Vehicle %s capacity of %s kg would be exceeded by shipment %s (%s kg requested in total)"
                                .formatted(trip.getVehicle().getPlateNumber(), capacity,
                                        shipment.getTrackingNumber(), loadedWeight));
            }

            TripShipment entry = TripShipment.builder()
                    .trip(trip)
                    .shipment(shipment)
                    .stopSequence(++nextSequence)
                    .build();
            trip.getManifest().add(entry);
        }

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse removeShipment(UUID tripId, UUID shipmentId) {
        Trip trip = findOrThrow(tripId);
        requirePlanned(trip, "unloaded");

        TripShipment entry = tripShipmentRepository.findByTripIdAndShipmentId(tripId, shipmentId)
                .orElseThrow(() -> new NotFoundException(
                        "Shipment %s is not on trip %s".formatted(shipmentId, trip.getTripCode())));

        trip.getManifest().remove(entry);
        resequenceManifest(trip);

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse dispatch(UUID id) {
        Trip trip = findOrThrow(id);
        requireTransition(trip, TripStatus.IN_TRANSIT);

        if (trip.getManifest().isEmpty()) {
            throw new BusinessRuleException(
                    "Trip %s has an empty manifest and cannot be dispatched".formatted(trip.getTripCode()));
        }
        if (!trip.getDriver().hasValidLicense(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Driver %s has an expired license".formatted(trip.getDriver().getEmployeeCode()));
        }

        Instant now = Instant.now();
        String actor = currentUserProvider.currentEmailOrSystem();

        trip.setStatus(TripStatus.IN_TRANSIT);
        trip.setActualDeparture(now);
        trip.getVehicle().setStatus(VehicleStatus.ON_TRIP);
        trip.getDriver().setStatus(DriverStatus.ON_TRIP);

        for (TripShipment entry : trip.getManifest()) {
            Shipment shipment = entry.getShipment();
            entry.setLoadedAt(now);

            // a shipment booked but never scanned at the hub is collected by this departure
            if (shipment.getStatus() == ShipmentStatus.CREATED) {
                shipment.transitionTo(ShipmentStatus.PICKED_UP,
                        "Collected for trip " + trip.getTripCode(),
                        trip.getOriginWarehouse(), null, actor, now);
            }
            shipment.transitionTo(ShipmentStatus.IN_TRANSIT,
                    "Departed on trip " + trip.getTripCode(),
                    trip.getOriginWarehouse(), null, actor, now);
        }

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse complete(UUID id) {
        Trip trip = findOrThrow(id);
        if (trip.getStatus() != TripStatus.IN_TRANSIT) {
            throw BusinessRuleException.illegalTransition(
                    "Trip " + trip.getTripCode(), trip.getStatus(), TripStatus.COMPLETED);
        }

        Instant now = Instant.now();
        String actor = currentUserProvider.currentEmailOrSystem();

        trip.setStatus(TripStatus.COMPLETED);
        trip.setActualArrival(now);
        trip.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        trip.getDriver().setStatus(DriverStatus.AVAILABLE);

        for (TripShipment entry : trip.getManifest()) {
            Shipment shipment = entry.getShipment();
            entry.setUnloadedAt(now);

            // shipments already delivered or failed en route keep the status the courier recorded
            if (shipment.getStatus() == ShipmentStatus.IN_TRANSIT) {
                shipment.transitionTo(ShipmentStatus.AT_DESTINATION_HUB,
                        "Arrived at " + trip.getDestinationWarehouse().getName(),
                        trip.getDestinationWarehouse(), null, actor, now);
            }
        }

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse cancel(UUID id) {
        Trip trip = findOrThrow(id);
        requireTransition(trip, TripStatus.CANCELLED);

        trip.setStatus(TripStatus.CANCELLED);
        trip.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        trip.getDriver().setStatus(DriverStatus.AVAILABLE);

        // the shipments keep their own status; they are simply free to join another trip
        trip.getManifest().clear();

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    private Trip findOrThrow(UUID id) {
        return tripRepository.findById(id).orElseThrow(() -> NotFoundException.of("Trip", id));
    }

    private void requirePlanned(Trip trip, String action) {
        if (!trip.getStatus().isManifestEditable()) {
            throw new BusinessRuleException(
                    "Trip %s is %s and can no longer be %s".formatted(trip.getTripCode(), trip.getStatus(), action));
        }
    }

    private void requireTransition(Trip trip, TripStatus target) {
        if (!trip.getStatus().canTransitionTo(target)) {
            throw BusinessRuleException.illegalTransition(
                    "Trip " + trip.getTripCode(), trip.getStatus(), target);
        }
    }

    /** Keeps stop numbers contiguous after a removal so the driver's sheet has no gaps. */
    private void resequenceManifest(Trip trip) {
        int sequence = 1;
        for (TripShipment entry : trip.getManifest()) {
            entry.setStopSequence(sequence++);
        }
    }

    private Warehouse resolveWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> NotFoundException.of("Warehouse", warehouseId));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("Warehouse %s is inactive".formatted(warehouse.getWarehouseCode()));
        }
        return warehouse;
    }

    private Vehicle resolveAvailableVehicle(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> NotFoundException.of("Vehicle", vehicleId));
        if (!vehicle.getStatus().isDispatchable()) {
            throw new ConflictException("Vehicle %s is %s and cannot be assigned"
                    .formatted(vehicle.getPlateNumber(), vehicle.getStatus()));
        }
        if (tripRepository.existsByVehicleIdAndStatusIn(vehicleId, OPEN_TRIP_STATUSES)) {
            throw new ConflictException("Vehicle %s is already committed to an open trip"
                    .formatted(vehicle.getPlateNumber()));
        }
        return vehicle;
    }

    private Driver resolveAvailableDriver(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> NotFoundException.of("Driver", driverId));
        if (!driver.getStatus().isDispatchable()) {
            throw new ConflictException("Driver %s is %s and cannot be assigned"
                    .formatted(driver.getEmployeeCode(), driver.getStatus()));
        }
        if (!driver.hasValidLicense(LocalDate.now())) {
            throw new BusinessRuleException("Driver %s has an expired license"
                    .formatted(driver.getEmployeeCode()));
        }
        if (tripRepository.existsByDriverIdAndStatusIn(driverId, OPEN_TRIP_STATUSES)) {
            throw new ConflictException("Driver %s is already committed to an open trip"
                    .formatted(driver.getEmployeeCode()));
        }
        return driver;
    }
}
