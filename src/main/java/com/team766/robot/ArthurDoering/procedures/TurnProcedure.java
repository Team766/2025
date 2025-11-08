package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;

public class TurnProcedure extends Procedure {

    private Drive drive;
    private double seconds;
    private double motorPower;

    public TurnProcedure(Drive drive, double seconds, double motorPower) {
        this.drive = reserve(drive);
        this.seconds = seconds;
        this.motorPower = motorPower;
    }

    public void run(Context context) {
        drive.turn_right(motorPower);
        context.waitForSeconds(seconds);
        drive.move_straight(0);
    }
}
