package com.team766.simulator.elements;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.sim.Pigeon2SimState;
import com.team766.simulator.interfaces.SimBody;

import edu.wpi.first.math.MathUtil;

public class Pigeon2Sim {
    private final Pigeon2SimState simState;

    private double previousYawRadians = 0.0;

    public Pigeon2Sim(Pigeon2 device) {
        simState = device.getSimState();
    }

    public void step(SimBody.State robotState) {
        final double yawRadians = robotState.position().getRotation().getZ();
        final double deltaYawRadians = MathUtil.angleModulus(yawRadians - previousYawRadians);
        previousYawRadians = yawRadians;

        simState.addYaw(Math.toDegrees(deltaYawRadians));
        simState.setRoll(Math.toDegrees(robotState.position().getRotation().getX()));
        simState.setPitch(Math.toDegrees(robotState.position().getRotation().getY()));
    }
}
