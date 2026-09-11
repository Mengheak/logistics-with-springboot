package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.entities.trip.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @EntityGraph(attributePaths = {"vehicle", "driver", "originWarehouse", "destinationWarehouse"})
    Optional<Trip> findWithDetailsById(UUID id);

    Optional<Trip> findByTripCode(String tripCode);

    boolean existsByTripCode(String tripCode);

    /** Used to reject double-booking a vehicle or driver that is already committed. */
    boolean existsByVehicleIdAndStatusIn(UUID vehicleId, Collection<TripStatus> statuses);

    boolean existsByDriverIdAndStatusIn(UUID driverId, Collection<TripStatus> statuses);

    @Query("""
            select t from Trip t
            where (:status is null or t.status = :status)
              and (:vehicleId is null or t.vehicle.id = :vehicleId)
              and (:driverId is null or t.driver.id = :driverId)
              and (:originWarehouseId is null or t.originWarehouse.id = :originWarehouseId)
            """)
    Page<Trip> search(@Param("status") TripStatus status,
                      @Param("vehicleId") UUID vehicleId,
                      @Param("driverId") UUID driverId,
                      @Param("originWarehouseId") UUID originWarehouseId,
                      Pageable pageable);
}
