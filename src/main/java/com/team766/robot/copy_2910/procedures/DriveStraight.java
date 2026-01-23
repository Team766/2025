package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;

public class DriveStraight extends Procedure {
    private SwerveDrive drive;

    public DriveStraight(SwerveDrive drive) {
        this.drive = reserve(drive);
    }

    @Override
    public void run(Context context) {
        // context.waitForSeconds(2);
        drive.controlRobotOriented(1, 0, 0);
        context.waitForSeconds(10);
        drive.controlRobotOriented(0, 0, 0);
    }
}
