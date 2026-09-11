package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCustomerCode(String customerCode);

    boolean existsByCustomerCode(String customerCode);

    Optional<Customer> findByUserAccountId(UUID userId);

    @Query("""
            select c from Customer c
            where (:keyword is null
                   or lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(c.customerCode) like lower(concat('%', :keyword, '%')))
              and (:active is null or c.active = :active)
            """)
    Page<Customer> search(@Param("keyword") String keyword,
                         @Param("active") Boolean active,
                         Pageable pageable);
}
