package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.entities.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Driver> findByUserAccountId(UUID userId);

    @Query("""
            select d from Driver d
            where (:status is null or d.status = :status)
              and (:warehouseId is null or d.baseWarehouse.id = :warehouseId)
              and (:keyword is null
                   or lower(d.fullName) like lower(concat('%', :keyword, '%'))
                   or lower(d.employeeCode) like lower(concat('%', :keyword, '%')))
            """)
    Page<Driver> search(@Param("status") DriverStatus status,
                        @Param("warehouseId") UUID warehouseId,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
