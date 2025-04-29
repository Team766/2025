package com.team766.robot.gatorade.procedures;

import com.team766.framework.InstantProcedure;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class StopDrive extends InstantProcedure {

    private final SwerveDrive drive;

    public StopDrive(SwerveDrive drive) {
        this.drive = reserve(drive);
    }

    public void run() {
        drive.stopDrive();
    }
}
