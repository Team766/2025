package com.team766.hal.mock;

import com.team766.hal.TimeOfFlightReader;
import java.util.Optional;

public class MockTimeOfFlight implements TimeOfFlightReader {
    private double distance = 0.0;
    private boolean valid = false;
    private double ambientSignal = 0.0;

    public MockTimeOfFlight() {}

    @Override
    public Optional<Double> getDistance() {
        return Optional.of(distance);
    }

    @Override
    public boolean wasLastMeasurementValid() {
        return valid;
    }

    @Override
    public Optional<Double> getAmbientSignal() {
        return Optional.of(ambientSignal);
    }

    @Override
    public void setRange(TimeOfFlightReader.Range range) {}

    public void setDistance(final double distance_) {
        this.distance = distance_;
    }

    public void setLastMeasurementValid(final boolean valid_) {
        this.valid = valid_;
    }

    public void setAmbientSignal(final double ambientSignal) {
        this.ambientSignal = ambientSignal;
    }
}
