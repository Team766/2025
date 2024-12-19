package com.team766.framework3.requests;

import com.team766.framework3.InstantRequest;
import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;

public class RequestForVoltage<M extends Mechanism<M, ?>> extends InstantRequest<M> {
    private final MotorController controller;
    private final double volts;

    public RequestForVoltage(MotorController controller, double volts) {
        this.controller = controller;
        this.volts = volts;
    }

    @Override
    public void runOnce() {
        controller.set(MotorController.ControlMode.Voltage, volts);
    }
}
