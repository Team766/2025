package com.team766.hal.mock;

import com.team766.hal.TimeOfFlightReader;
import java.util.Optional;

public class MockTimeOfFlight implements TimeOfFlightReader {
    private Optional<Double> distance = Optional.empty();
    private boolean valid = false;
    private Optional<Double> ambientSignal = Optional.empty();

    public MockTimeOfFlight() {}

    @Override
    public Optional<Double> getDistance() {
        return distance;
    }

    @Override
    public boolean wasLastMeasurementValid() {
        return valid;
    }

    @Override
    public Optional<Double> getAmbientSignal() {
        return ambientSignal;
    }

    @Override
    public void setRange(TimeOfFlightReader.Range range) {}

    public void setDistance(final double distance_) {
        this.distance = Optional.of(distance_);
    }

    public void setLastMeasurementValid(final boolean valid_) {
        this.valid = valid_;
    }

    public void setAmbientSignal(final double ambientSignal) {
        this.ambientSignal = Optional.of(ambientSignal);
    }
}
