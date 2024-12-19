package com.team766.framework3.requests;

import com.team766.framework3.AbstractRequest;
import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;
import java.util.function.Supplier;

public class RequestForPositionControl<M extends Mechanism<M, ?>> extends AbstractRequest<M> {
    private final MotorController controller;
    private final double targetPosition;
    private final double positionErrorThreshold;
    private final double velocityThreshold;
    private final Supplier<Double> arbitraryFeedForward;

    public RequestForPositionControl(
            MotorController controller,
            double targetPosition,
            double positionErrorThreshold,
            double velocityThreshold,
            double arbitraryFeedForward) {
        this.controller = controller;
        this.targetPosition = targetPosition;
        this.positionErrorThreshold = positionErrorThreshold;
        this.velocityThreshold = velocityThreshold;
        this.arbitraryFeedForward = () -> arbitraryFeedForward;
    }

    public RequestForPositionControl(
            MotorController controller,
            double targetPosition,
            double positionErrorThreshold,
            double velocityThreshold,
            Supplier<Double> arbitraryFeedForward) {
        this.controller = controller;
        this.targetPosition = targetPosition;
        this.positionErrorThreshold = positionErrorThreshold;
        this.velocityThreshold = velocityThreshold;
        this.arbitraryFeedForward = arbitraryFeedForward;
    }

    @Override
    public boolean run() {
        controller.applyPIDConfig(); // TODO: Skip this in competition mode
        controller.set(
                MotorController.ControlMode.Position, targetPosition, arbitraryFeedForward.get());
        return Math.abs(targetPosition - controller.getSensorPosition()) <= positionErrorThreshold
                && Math.abs(controller.getSensorVelocity()) <= velocityThreshold;
    }
}
