package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;

public class TurnProcedure extends Procedure {

    private Drive drive;
    private double seconds;

    public TurnProcedure(Drive myDrive, double seconds) {
        drive = reserve(myDrive);
        this.seconds = seconds;
    }

    public void run(Context context) {
        drive.turn_right(-1);
        context.waitForSeconds(seconds);
        drive.turn_right(0);
    }
}
