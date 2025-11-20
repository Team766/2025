package com.team766.robot.mark.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mark.mechanisms.Drive;
import com.team766.robot.mark.mechanisms.MovingMotor;

public class Autonomous extends Procedure {
    private MovingMotor motor;
    private Drive drive;

    public Autonomous(MovingMotor my_motor, Drive my_drive) {
        motor = reserve(my_motor);
        drive = reserve(my_drive);
    }

    public void run(Context context) {}
}
