package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;

public class DriveDistance extends Procedure {
    private final Drive drive;

    public DriveDistance(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        drive.resetEncoders();

        drive.setDrivePower(0.25, 0.25);

        waitForStatusMatching(context, Drive.DriveStatus.class, s -> s.distance() >= 60.0);

        drive.setDrivePower(0.0, 0.0);
    }
}
