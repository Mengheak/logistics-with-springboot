package com.chheang.mengheak.logisticsapis.common.enums;

public enum VehicleStatus {
    AVAILABLE,
    ON_TRIP,
    MAINTENANCE,
    RETIRED;

    public boolean isDispatchable() {
        return this == AVAILABLE;
    }
}
