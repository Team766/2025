package com.team766.hal;

public interface TimeOfFlightReader {
    enum Type {
        CANRange
        // TODO: add support for REVDistanceSensor
    };

    enum Range {
        Short,
        Long
    }

    /**
     * Set the range of the sensor.
     */
    void setRange(Range range);

    /**
     * Return the distance to the target in mm.
     */
    double getDistance();

    /**
     * Return whether or not the last measurement was valid.
     */
    boolean wasLastMeasurementValid();
}
