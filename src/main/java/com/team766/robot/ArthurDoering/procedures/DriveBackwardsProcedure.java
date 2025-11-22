package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;

public class DriveBackwardsProcedure extends Procedure {

    private Drive drive;
    private double seconds;

    public DriveBackwardsProcedure(Drive myDrive, double seconds) {
        drive = reserve(myDrive);
        this.seconds = seconds;
    }

    public void run(Context context) {
        drive.move_straight(0.8);
        context.waitForSeconds(seconds);
        drive.move_straight(0);
    }
}
