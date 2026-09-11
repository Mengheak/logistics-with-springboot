package com.chheang.mengheak.logisticsapis.entities.shipment;

import com.chheang.mengheak.logisticsapis.common.enums.ServiceLevel;
import com.chheang.mengheak.logisticsapis.common.enums.ShipmentStatus;
import com.chheang.mengheak.logisticsapis.entities.Customer;
import com.chheang.mengheak.logisticsapis.entities.Warehouse;
import com.chheang.mengheak.logisticsapis.entities.base.AuditableEntity;
import com.chheang.mengheak.logisticsapis.entities.embeddable.Address;
import com.chheang.mengheak.logisticsapis.exception.BusinessRuleException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The consignment a customer books: one tracking number, one or more parcels. */
@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_tracking_number", columnList = "tracking_number"),
        @Index(name = "idx_shipments_status", columnList = "status"),
        @Index(name = "idx_shipments_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Shipment extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "shipment_id")
    private UUID id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 30)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "pickup_line1", length = 160)),
            @AttributeOverride(name = "line2", column = @Column(name = "pickup_line2", length = 160)),
            @AttributeOverride(name = "city", column = @Column(name = "pickup_city", length = 80)),
            @AttributeOverride(name = "province", column = @Column(name = "pickup_province", length = 80)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "pickup_postal_code", length = 20)),
            @AttributeOverride(name = "countryCode", column = @Column(name = "pickup_country_code", length = 2)),
            @AttributeOverride(name = "contactName", column = @Column(name = "pickup_contact_name", length = 120)),
            @AttributeOverride(name = "contactPhone", column = @Column(name = "pickup_contact_phone", length = 30))
    })
    private Address pickupAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "delivery_line1", length = 160)),
            @AttributeOverride(name = "line2", column = @Column(name = "delivery_line2", length = 160)),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_city", length = 80)),
            @AttributeOverride(name = "province", column = @Column(name = "delivery_province", length = 80)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "delivery_postal_code", length = 20)),
            @AttributeOverride(name = "countryCode", column = @Column(name = "delivery_country_code", length = 2)),
            @AttributeOverride(name = "contactName", column = @Column(name = "delivery_contact_name", length = 120)),
            @AttributeOverride(name = "contactPhone", column = @Column(name = "delivery_contact_phone", length = 30))
    })
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_level", nullable = false, length = 20)
    @Builder.Default
    private ServiceLevel serviceLevel = ServiceLevel.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    /** Sum of the item weights, recalculated whenever the parcel list changes. */
    @Column(name = "total_weight_kg", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal totalWeightKg = BigDecimal.ZERO;

    @Column(name = "declared_value", precision = 12, scale = 2)
    private BigDecimal declaredValue;

    /** Cash to collect from the recipient on delivery; null when prepaid. */
    @Column(name = "cod_amount", precision = 12, scale = 2)
    private BigDecimal codAmount;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShipmentItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC")
    @Builder.Default
    private List<TrackingEvent> trackingEvents = new ArrayList<>();

    public void addItem(ShipmentItem item) {
        item.setShipment(this);
        this.items.add(item);
    }

    public void addTrackingEvent(TrackingEvent event) {
        event.setShipment(this);
        this.trackingEvents.add(event);
    }

    /** Keeps {@code totalWeightKg} in step with the parcel list. */
    public void recalculateTotalWeight() {
        this.totalWeightKg = items.stream()
                .map(ShipmentItem::grossWeightKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * The single place a shipment's status changes. Validates the transition, stamps the delivery
     * time on arrival and appends the matching tracking event, so no caller can move a shipment
     * without leaving a trace.
     */
    public TrackingEvent transitionTo(ShipmentStatus target,
                                      String description,
                                      Warehouse location,
                                      String locationNote,
                                      String recordedBy,
                                      Instant occurredAt) {
        if (!status.canTransitionTo(target)) {
            throw BusinessRuleException.illegalTransition(
                    "Shipment " + trackingNumber, status, target);
        }

        this.status = target;
        if (target == ShipmentStatus.DELIVERED) {
            this.deliveredAt = occurredAt;
        }

        TrackingEvent event = TrackingEvent.builder()
                .status(target)
                .description(description)
                .locationWarehouse(location)
                .locationNote(locationNote)
                .recordedBy(recordedBy)
                .occurredAt(occurredAt)
                .build();
        addTrackingEvent(event);
        return event;
    }
}
