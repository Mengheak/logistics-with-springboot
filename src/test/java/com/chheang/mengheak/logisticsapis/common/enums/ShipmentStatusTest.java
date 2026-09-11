package com.chheang.mengheak.logisticsapis.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The transition table is the rule the whole shipment lifecycle leans on, so it is pinned here. */
class ShipmentStatusTest {

    @Test
    void allowsTheHappyPath() {
        assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.PICKED_UP)).isTrue();
        assertThat(ShipmentStatus.PICKED_UP.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isTrue();
        assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.AT_DESTINATION_HUB)).isTrue();
        assertThat(ShipmentStatus.AT_DESTINATION_HUB.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isTrue();
        assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.DELIVERED)).isTrue();
    }

    @Test
    void rejectsSkippingAheadToDelivered() {
        assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
        assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
    }

    @Test
    void terminalStatusesCannotMove() {
        assertThat(ShipmentStatus.DELIVERED.isTerminal()).isTrue();
        assertThat(ShipmentStatus.CANCELLED.isTerminal()).isTrue();
        assertThat(ShipmentStatus.RETURNED.isTerminal()).isTrue();
        assertThat(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isFalse();
    }

    @Test
    void onlyPreTransitShipmentsCanJoinATrip() {
        assertThat(ShipmentStatus.CREATED.isAssignable()).isTrue();
        assertThat(ShipmentStatus.AT_ORIGIN_HUB.isAssignable()).isTrue();
        assertThat(ShipmentStatus.IN_TRANSIT.isAssignable()).isFalse();
        assertThat(ShipmentStatus.DELIVERED.isAssignable()).isFalse();
    }

    @Test
    void cancellingIsOnlyPossibleBeforeDeparture() {
        assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.CANCELLED)).isTrue();
        assertThat(ShipmentStatus.AT_ORIGIN_HUB.canTransitionTo(ShipmentStatus.CANCELLED)).isTrue();
        assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.CANCELLED)).isFalse();
    }
}
