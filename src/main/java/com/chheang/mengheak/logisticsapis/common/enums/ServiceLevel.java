package com.chheang.mengheak.logisticsapis.common.enums;

public enum ServiceLevel {
    ECONOMY(7),
    STANDARD(4),
    EXPRESS(2),
    SAME_DAY(0);

    private final int transitDays;

    ServiceLevel(int transitDays) {
        this.transitDays = transitDays;
    }

    public int getTransitDays() {
        return transitDays;
    }
}
