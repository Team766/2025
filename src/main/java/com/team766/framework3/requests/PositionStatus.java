package com.team766.framework3.requests;

import com.team766.framework3.Status;

public interface PositionStatus extends Status {
    double position();

    double positionTolerance();
}
