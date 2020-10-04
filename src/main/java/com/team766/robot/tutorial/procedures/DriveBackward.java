package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;

public class DriveBackward extends Procedure {
    private final Drive drive;

    public DriveBackward(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        drive.setDrivePower(-0.25, -0.25);

        context.waitForSeconds(2.7);

        drive.setDrivePower(0.0, 0.0);
    }
}
