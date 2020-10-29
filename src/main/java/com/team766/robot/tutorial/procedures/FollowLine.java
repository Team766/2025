package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;
import com.team766.robot.tutorial.mechanisms.LineSensors.LineSensorsStatus;

public class FollowLine extends Procedure {
    private final Drive drive;

    public FollowLine(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        while (true) {
            LineSensorsStatus lineSensors = getStatusOrThrow(LineSensorsStatus.class);
            // Add line following code here
            double steering = 0;
            if (lineSensors.left()) {
                steering = -0.45;
            } else if (lineSensors.right()) {
                steering = 0.45;
            }
            if (lineSensors.center()) {
                steering /= 4;
            }
            drive.setArcadeDrivePower(0.20, steering);

            context.yield();
        }
    }
}
