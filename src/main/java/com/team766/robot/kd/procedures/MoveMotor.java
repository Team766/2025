package com.team766.robot.kd.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.kd.mechanisms.MovingMotor;

public class MoveMotor extends Procedure {
    private MovingMotor motor;

    public MoveMotor(MovingMotor my_motor) {
        motor = reserve(my_motor);
    }

    public void run(Context context) {
        motor.move(0.6);
        context.waitForSeconds(5);
        motor.stop();
    }
}
