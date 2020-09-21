package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;

public class MyAutonomous extends Procedure {
    private final Drive drive;

    public MyAutonomous(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        context.runParallel(new DriveStraight(drive), new RaiseArm());
    }
}
