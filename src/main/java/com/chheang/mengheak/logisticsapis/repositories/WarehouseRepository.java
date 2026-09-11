package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    @Query("""
            select w from Warehouse w
            where (:keyword is null
                   or lower(w.name) like lower(concat('%', :keyword, '%'))
                   or lower(w.warehouseCode) like lower(concat('%', :keyword, '%')))
              and (:active is null or w.active = :active)
            """)
    Page<Warehouse> search(@Param("keyword") String keyword,
                          @Param("active") Boolean active,
                          Pageable pageable);
}
