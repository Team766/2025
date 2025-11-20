package com.team766.robot.StephenCheung.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.StephenCheung.mechanisms.Drive;

public class DriveProcedure extends Procedure {

    private Drive drive;
    private double seconds;

    public DriveProcedure(Drive drive, double seconds) {
        this.drive = reserve(drive);
        this.seconds = seconds;
    }

    public void run(Context context) {
        drive.move_straight(1);
        context.waitForSeconds(seconds);
        drive.move_straight(0);
    }
}
