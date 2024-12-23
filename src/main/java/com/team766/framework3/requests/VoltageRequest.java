package com.team766.framework3.requests;

import com.team766.framework3.Request;

public record VoltageRequest(double targetVoltage) implements Request<VoltageStatus> {
    // Almost all systems on our robot work on the scale of 0-12 V.
    // 0.1 V seems like a reasonable tolerance for that scale.
    private static final double VOLTAGE_TOLERANCE = 0.1;

    @Override
    public boolean isDone(VoltageStatus status) {
        return Math.abs(targetVoltage - status.voltage()) <= VOLTAGE_TOLERANCE;
    }
}
