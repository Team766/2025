package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;

public class TurnAngle extends Procedure {
    private final Drive drive;

    public TurnAngle(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        drive.resetGyro();

        drive.setArcadeDrivePower(0.0, 0.25);
        waitForStatusMatching(context, Drive.DriveStatus.class, s -> s.angle() <= -45);
        drive.setArcadeDrivePower(0.0, 0.10);
        waitForStatusMatching(context, Drive.DriveStatus.class, s -> s.angle() <= -80);
        drive.setArcadeDrivePower(0.0, 0.01);
        waitForStatusMatching(context, Drive.DriveStatus.class, s -> s.angle() <= -90);

        drive.setDrivePower(0.0, 0.0);
    }
}
