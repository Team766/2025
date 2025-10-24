package com.team766.simulator.elements;

/* Code retained for reference. TODO: Remove this

import com.team766.simulator.ProgramInterface;
import com.team766.simulator.interfaces.SimBody;

import edu.wpi.first.math.MathUtil;

public class GyroSim {
    private double previousYawRadians = 0.0;

    public void step(SimBody.State robotState) {
        final double yawRadians = robotState.position().getRotation().getZ();
        final double deltaYawRadians = MathUtil.angleModulus(yawRadians - previousYawRadians);
        previousYawRadians = yawRadians;

        ProgramInterface.gyro.angle += Math.toDegrees(deltaYawRadians);
        ProgramInterface.gyro.rate = Math.toDegrees(robotState.velocity().rz);
    }
}
*/