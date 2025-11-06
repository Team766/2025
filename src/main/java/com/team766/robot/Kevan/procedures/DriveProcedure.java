package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;

public class DriveProcedure extends Procedure {

    private Drive drive;
    private double seconds;

    public DriveProcedure(Drive myDrive, double seconds) {
        drive = reserve(myDrive);
        this.seconds = seconds;
    }

    public void run(Context context) {
        drive.move_straight(1);
        context.waitForSeconds(seconds);
        drive.move_straight(0);
    }
}
