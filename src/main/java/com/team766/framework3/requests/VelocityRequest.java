package com.team766.framework3.requests;

import com.team766.framework3.Request;

public record VelocityRequest(double targetVelocity) implements Request<VelocityStatus> {
    @Override
    public boolean isDone(VelocityStatus status) {
        return Math.abs(targetVelocity - status.velocity()) <= status.velocityTolerance();
    }
}
