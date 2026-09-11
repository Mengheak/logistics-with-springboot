package com.chheang.mengheak.logisticsapis.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TripStatusTest {

    @Test
    void plannedTripsCanDepartOrBeCancelled() {
        assertThat(TripStatus.PLANNED.canTransitionTo(TripStatus.IN_TRANSIT)).isTrue();
        assertThat(TripStatus.PLANNED.canTransitionTo(TripStatus.CANCELLED)).isTrue();
    }

    @Test
    void aDepartedTripCannotBeCancelled() {
        assertThat(TripStatus.IN_TRANSIT.canTransitionTo(TripStatus.CANCELLED)).isFalse();
        assertThat(TripStatus.IN_TRANSIT.canTransitionTo(TripStatus.COMPLETED)).isTrue();
    }

    @Test
    void onlyPlannedTripsAcceptManifestEdits() {
        assertThat(TripStatus.PLANNED.isManifestEditable()).isTrue();
        assertThat(TripStatus.IN_TRANSIT.isManifestEditable()).isFalse();
        assertThat(TripStatus.COMPLETED.isManifestEditable()).isFalse();
    }

    @Test
    void openStatusesAreTheOnesHoldingResources() {
        assertThat(TripStatus.openStatuses())
                .containsExactlyInAnyOrder(TripStatus.PLANNED, TripStatus.IN_TRANSIT);
    }
}
