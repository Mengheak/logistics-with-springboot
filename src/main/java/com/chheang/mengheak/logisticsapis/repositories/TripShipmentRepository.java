package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.entities.trip.TripShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripShipmentRepository extends JpaRepository<TripShipment, UUID> {

    Optional<TripShipment> findByTripIdAndShipmentId(UUID tripId, UUID shipmentId);

    List<TripShipment> findAllByTripIdOrderByStopSequenceAsc(UUID tripId);

    /** True when the shipment already sits on a trip that has not finished yet. */
    boolean existsByShipmentIdAndTripStatusIn(UUID shipmentId, Collection<TripStatus> statuses);

    @Query("select coalesce(max(ts.stopSequence), 0) from TripShipment ts where ts.trip.id = :tripId")
    int findMaxStopSequence(@Param("tripId") UUID tripId);
}
