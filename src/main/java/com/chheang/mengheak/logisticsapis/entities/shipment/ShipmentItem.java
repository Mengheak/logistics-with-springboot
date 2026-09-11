package com.chheang.mengheak.logisticsapis.entities.shipment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
import java.util.UUID;

/** One parcel line inside a shipment. */
@Entity
@Table(name = "shipment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShipmentItem {

    @Id
    @GeneratedValue
    @Column(name = "shipment_item_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 1;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "length_cm", precision = 8, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 8, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Column(nullable = false)
    @Builder.Default
    private boolean fragile = false;

    /** Unit weight multiplied by quantity. */
    public BigDecimal grossWeightKg() {
        if (weightKg == null) {
            return BigDecimal.ZERO;
        }
        return weightKg.multiply(BigDecimal.valueOf(quantity));
    }
}
