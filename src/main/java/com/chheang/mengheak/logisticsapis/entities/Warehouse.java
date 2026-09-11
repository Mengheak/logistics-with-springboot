package com.chheang.mengheak.logisticsapis.entities;

import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import com.chheang.mengheak.logisticsapis.entities.embeddable.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/** A hub, depot or sorting facility that shipments pass through. */
@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_warehouses_code", columnList = "warehouse_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Warehouse extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "warehouse_id")
    private UUID id;

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 30)
    private String warehouseCode;

    @Column(nullable = false, length = 160)
    private String name;

    @Embedded
    private Address address;

    /** Storage ceiling in cubic metres, used to sanity-check inbound volume. */
    @Column(name = "capacity_m3")
    private Double capacityM3;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
