package com.chheang.mengheak.logisticsapis.entities;

import com.chheang.mengheak.logisticsapis.common.enums.DriverStatus;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_drivers_employee_code", columnList = "employee_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Driver extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "driver_id")
    private UUID id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(name = "license_number", nullable = false, unique = true, length = 40)
    private String licenseNumber;

    @Column(name = "license_expiry", nullable = false)
    private LocalDate licenseExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_warehouse_id")
    private Warehouse baseWarehouse;

    /** Optional login, set when the driver uses the mobile app. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserAccount userAccount;

    public boolean hasValidLicense(LocalDate on) {
        return licenseExpiry != null && !licenseExpiry.isBefore(on);
    }
}
