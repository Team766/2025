package com.team766.framework3.requests;

import com.team766.framework3.InstantRequest;
import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;

public class RequestForStop<M extends Mechanism<M, ?>> extends InstantRequest<M> {
    private final MotorController controller;

    public RequestForStop(MotorController controller) {
        this.controller = controller;
    }

    @Override
    public void runOnce() {
        controller.stopMotor();
    }
}
