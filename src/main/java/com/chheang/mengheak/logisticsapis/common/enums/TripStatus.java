package com.chheang.mengheak.logisticsapis.common.enums;

import java.util.Set;

public enum TripStatus {
    PLANNED,
    IN_TRANSIT,
    COMPLETED,
    CANCELLED;

    public Set<TripStatus> allowedNextStatuses() {
        return switch (this) {
            case PLANNED -> Set.of(IN_TRANSIT, CANCELLED);
            // once the vehicle has left, the only way out is to arrive and close the trip
            case IN_TRANSIT -> Set.of(COMPLETED);
            case COMPLETED, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(TripStatus target) {
        return allowedNextStatuses().contains(target);
    }

    public boolean isTerminal() {
        return allowedNextStatuses().isEmpty();
    }

    /** Manifest edits are only allowed before the vehicle leaves the hub. */
    public boolean isManifestEditable() {
        return this == PLANNED;
    }

    /** Statuses where a vehicle, driver or shipment is still committed to this trip. */
    public static Set<TripStatus> openStatuses() {
        return Set.of(PLANNED, IN_TRANSIT);
    }
}
