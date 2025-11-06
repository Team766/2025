package com.team766.robot.Kevan.Not_Used;

import com.team766.framework.Context;
import com.team766.framework.Procedure;

public class MoveMotor extends Procedure {
    private MovingMotor motor;

    public MoveMotor(MovingMotor myMotor) {
        motor = reserve(myMotor);
    }

    public void run(Context context) {
        motor.move(1);
        context.waitForSeconds(5);
        motor.move(0);
    }
}
