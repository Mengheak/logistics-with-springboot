package com.chheang.mengheak.logisticsapis.common.enums;

import java.util.Set;

/**
 * Lifecycle of a shipment. The allowed transitions live next to the enum so every
 * caller validates against the same rule set.
 */
public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    AT_ORIGIN_HUB,
    IN_TRANSIT,
    AT_DESTINATION_HUB,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_DELIVERY,
    RETURNED,
    CANCELLED;

    public Set<ShipmentStatus> allowedNextStatuses() {
        return switch (this) {
            case CREATED -> Set.of(PICKED_UP, CANCELLED);
            case PICKED_UP -> Set.of(AT_ORIGIN_HUB, IN_TRANSIT, CANCELLED);
            case AT_ORIGIN_HUB -> Set.of(IN_TRANSIT, CANCELLED);
            case IN_TRANSIT -> Set.of(AT_DESTINATION_HUB, OUT_FOR_DELIVERY, FAILED_DELIVERY);
            case AT_DESTINATION_HUB -> Set.of(OUT_FOR_DELIVERY, IN_TRANSIT);
            case OUT_FOR_DELIVERY -> Set.of(DELIVERED, FAILED_DELIVERY);
            case FAILED_DELIVERY -> Set.of(OUT_FOR_DELIVERY, AT_DESTINATION_HUB, RETURNED);
            case DELIVERED, RETURNED, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(ShipmentStatus target) {
        return allowedNextStatuses().contains(target);
    }

    public boolean isTerminal() {
        return allowedNextStatuses().isEmpty();
    }

    /** A shipment already moving cannot be re-assigned to another trip. */
    public boolean isAssignable() {
        return this == CREATED || this == PICKED_UP || this == AT_ORIGIN_HUB || this == AT_DESTINATION_HUB;
    }
}
