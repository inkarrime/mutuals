package com.mutuals.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilsTest {

    @Test
    void samePointHasZeroDistance() {
        assertThat(GeoUtils.distanceMeters(-12.1350, -77.0226, -12.1350, -77.0226)).isZero();
    }

    @Test
    void computesShortDistancesAccurately() {
        double distance = GeoUtils.distanceMeters(-12.1350, -77.0226, -12.1359, -77.0226);
        assertThat(distance).isCloseTo(100.0, within(1.0));
    }
}
