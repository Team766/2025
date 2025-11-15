package com.team766.robot.Jordan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Jordan.mechanisms.MovingMotor;

public class MoveMotorProcedure extends Procedure {
    private MovingMotor motor;

    public MoveMotorProcedure(MovingMotor myMotor) {
        motor = reserve(myMotor);
    }

    @Override
    public void run(Context context){
        motor.setMotorPower(1);
        context.waitForSeconds(5);
        motor.setMotorPower(0);
    }
}
