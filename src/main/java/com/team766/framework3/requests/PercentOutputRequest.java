package com.team766.framework3.requests;

import com.team766.framework3.Request;

public record PercentOutputRequest(double targetPercentOutput)
        implements Request<PercentOutputStatus> {
    @Override
    public boolean isDone(PercentOutputStatus status) {
        return status.percentOutput() == targetPercentOutput;
    }
}
