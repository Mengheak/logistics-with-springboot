package com.chheang.mengheak.logisticsapis.common.enums;

public enum DriverStatus {
    AVAILABLE,
    ON_TRIP,
    OFF_DUTY,
    SUSPENDED;

    public boolean isDispatchable() {
        return this == AVAILABLE;
    }
}
