package com.chheang.mengheak.logisticsapis.entities.shipment;

import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit trail behind the public tracking page. One row per status change,
 * never updated or deleted once written.
 */
@Entity
@Table(name = "tracking_events", indexes = {
        @Index(name = "idx_tracking_events_shipment", columnList = "shipment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrackingEvent {

    @Id
    @GeneratedValue
    @Column(name = "tracking_event_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(length = 300)
    private String description;

    /** Hub where the scan happened; null for on-road events. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_warehouse_id")
    private Warehouse locationWarehouse;

    @Column(name = "location_note", length = 160)
    private String locationNote;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** Email of the operator that recorded the scan, or "system" for automatic events. */
    @Column(name = "recorded_by", length = 120)
    private String recordedBy;
}
