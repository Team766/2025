package com.team766.hal;

import java.util.Optional;

public interface TimeOfFlightReader {
    enum Type {
        CANRange
        // TODO: add support for PlayingWithFusionTimeOfFlight
    };

    enum Range {
        /* Short range detection typically handles brighter lighting conditions better. */
        Short,
        /* Long range detection can handle longer ranges but performs best in darker conditions. */
        Long
    }

    /**
     * Set the range of the sensor.
     */
    void setRange(Range range);

    /**
     * Return the distance to the target in m.
     * If the sensor is unable to measure the distance, returns an empty Optional.
     */
    Optional<Double> getDistance();

    /**
     * Return whether or not the last measurement was valid.
     */
    boolean wasLastMeasurementValid();

    /**
     * Return the level of ambient (infrared) light detected by the sensor.
     *
     */
    Optional<Double> getAmbientSignal();
}
