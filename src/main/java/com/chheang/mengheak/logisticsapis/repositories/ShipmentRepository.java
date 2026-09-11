package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.entities.shipment.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @EntityGraph(attributePaths = {"customer", "originWarehouse", "destinationWarehouse", "items"})
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @EntityGraph(attributePaths = {"customer", "originWarehouse", "destinationWarehouse", "items"})
    Optional<Shipment> findWithDetailsById(UUID id);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("""
            select s from Shipment s
            where (:status is null or s.status = :status)
              and (:customerId is null or s.customer.id = :customerId)
              and (:originWarehouseId is null or s.originWarehouse.id = :originWarehouseId)
              and (:destinationWarehouseId is null or s.destinationWarehouse.id = :destinationWarehouseId)
              and (:keyword is null or lower(s.trackingNumber) like lower(concat('%', :keyword, '%')))
            """)
    Page<Shipment> search(@Param("status") ShipmentStatus status,
                          @Param("customerId") UUID customerId,
                          @Param("originWarehouseId") UUID originWarehouseId,
                          @Param("destinationWarehouseId") UUID destinationWarehouseId,
                          @Param("keyword") String keyword,
                          Pageable pageable);
}
