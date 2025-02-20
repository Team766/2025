package com.team766.hal.mock;

import com.team766.hal.TimeOfFlightReader;

public class MockTimeOfFlight implements TimeOfFlightReader {
    private double distance = 0.0;
    private boolean valid = false;

    public MockTimeOfFlight() {}

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public boolean wasLastMeasurementValid() {
        return valid;
    }

    @Override
    public void setRange(TimeOfFlightReader.Range range) {}

    public void setDistance(final double distance_) {
        this.distance = distance_;
    }

    public void setLastMeasurementValid(final boolean valid_) {
        this.valid = valid_;
    }
}
