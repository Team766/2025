package com.team766.framework3.requests;

import com.team766.framework3.AbstractRequest;
import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;

public class RequestForVelocityControl<M extends Mechanism<M, ?>> extends AbstractRequest<M> {
    private final MotorController controller;
    private final double targetVelocity;
    private final double velocityErrorThreshold;
    private boolean firstTime = true;

    public RequestForVelocityControl(
            MotorController controller, double targetVelocity, double velocityErrorThreshold) {
        this.controller = controller;
        this.targetVelocity = targetVelocity;
        this.velocityErrorThreshold = velocityErrorThreshold;
    }

    @Override
    public boolean run() {
        controller.applyPIDConfig(); // TODO: Skip this in competition mode
        if (firstTime) {
            controller.set(MotorController.ControlMode.Velocity, targetVelocity);
            firstTime = false;
        }
        return Math.abs(targetVelocity - controller.getSensorVelocity()) <= velocityErrorThreshold;
    }
}
