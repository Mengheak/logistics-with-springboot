package com.chheang.mengheak.logisticsapis.entities.trip;

import com.chheang.mengheak.logisticsapis.common.enums.TripStatus;
import com.chheang.mengheak.logisticsapis.entities.Driver;
import com.chheang.mengheak.logisticsapis.entities.Vehicle;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One vehicle + driver moving a manifest of shipments between two hubs. */
@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trips_code", columnList = "trip_code"),
        @Index(name = "idx_trips_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Trip extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "trip_id")
    private UUID id;

    @Column(name = "trip_code", nullable = false, unique = true, length = 30)
    private String tripCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.PLANNED;

    @Column(name = "scheduled_departure", nullable = false)
    private Instant scheduledDeparture;

    @Column(name = "scheduled_arrival")
    private Instant scheduledArrival;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopSequence ASC")
    @Builder.Default
    private List<TripShipment> manifest = new ArrayList<>();

    /** Total weight currently loaded, used to check the vehicle is not over capacity. */
    public BigDecimal manifestWeightKg() {
        return manifest.stream()
                .map(entry -> entry.getShipment().getTotalWeightKg())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
