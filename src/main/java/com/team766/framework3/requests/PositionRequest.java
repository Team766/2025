package com.team766.framework3.requests;

import com.team766.framework3.Request;

public record PositionRequest(double targetPosition) implements Request<PositionStatus> {
    @Override
    public boolean isDone(PositionStatus status) {
        if (status instanceof VelocityStatus velocityStatus
                && Math.abs(velocityStatus.velocity()) > velocityStatus.velocityTolerance()) {
            return false;
        }
        return Math.abs(targetPosition - status.position()) <= status.positionTolerance();
    }
}
