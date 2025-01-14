package com.team766.hal.wpilib;

import com.team766.hal.EncoderReader;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public class REVThroughBoreDutyCycleEncoder extends DutyCycleEncoder implements EncoderReader {
    private double offset = 0.0;
    private double distancePerRotation = 1.0;

    public REVThroughBoreDutyCycleEncoder(int channel) {
        super(channel);
        setDutyCycleRange(1. / 1025., 1024. / 1025.);
    }

    @Override
    public void reset() {
        offset = get();
    }

    @Override
    public double getRate() {
        throw new UnsupportedOperationException("getRate() not supported.");
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public void setDistancePerPulse(double distancePerPulse) {
        distancePerRotation = distancePerPulse;
    }

    @Override
    public double get() {
        return super.get() - offset;
    }

    @Override
    public double getDistance() {
        return get() * distancePerRotation;
    }

    public double getWithoutOffset() {
        return super.get();
    }
}
