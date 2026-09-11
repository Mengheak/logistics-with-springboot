package com.chheang.mengheak.logisticsapis.entities;

import com.chheang.mengheak.logisticsapis.common.enums.VehicleStatus;
import com.chheang.mengheak.logisticsapis.common.enums.VehicleType;
import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicles_plate", columnList = "plate_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Vehicle extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "vehicle_id")
    private UUID id;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(name = "capacity_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacityKg;

    @Column(name = "capacity_m3", precision = 10, scale = 2)
    private BigDecimal capacityM3;

    @Column(name = "registration_expiry")
    private LocalDate registrationExpiry;

    /** Depot the vehicle returns to between trips. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_warehouse_id")
    private Warehouse homeWarehouse;
}
