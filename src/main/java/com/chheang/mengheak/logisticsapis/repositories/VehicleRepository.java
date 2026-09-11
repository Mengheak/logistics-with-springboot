package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.entities.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    @Query("""
            select v from Vehicle v
            where (:status is null or v.status = :status)
              and (:type is null or v.type = :type)
              and (:warehouseId is null or v.homeWarehouse.id = :warehouseId)
            """)
    Page<Vehicle> search(@Param("status") VehicleStatus status,
                         @Param("type") VehicleType type,
                         @Param("warehouseId") UUID warehouseId,
                         Pageable pageable);
}
