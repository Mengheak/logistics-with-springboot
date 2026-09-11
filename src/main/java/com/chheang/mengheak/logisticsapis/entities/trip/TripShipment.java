package com.chheang.mengheak.logisticsapis.entities.trip;

import com.chheang.mengheak.logisticsapis.entities.shipment.Shipment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Join entity between a trip and a shipment. Modelled explicitly rather than as a plain
 * many-to-many because a manifest line carries its own stop order and load timestamp.
 */
@Entity
@Table(name = "trip_shipments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_trip_shipment", columnNames = {"trip_id", "shipment_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TripShipment {

    @Id
    @GeneratedValue
    @Column(name = "trip_shipment_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    /** Drop order along the route, 1-based. */
    @Column(name = "stop_sequence", nullable = false)
    private int stopSequence;

    @Column(name = "loaded_at")
    private Instant loadedAt;

    @Column(name = "unloaded_at")
    private Instant unloadedAt;
}
