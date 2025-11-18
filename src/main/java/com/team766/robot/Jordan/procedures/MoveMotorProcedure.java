package com.team766.robot.Jordan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Jordan.mechanisms.*;

public class MoveMotorProcedure extends Procedure {
    private MovingMotor motor;

    public MoveMotorProcedure(MovingMotor myMotor) {
        motor = reserve(myMotor);
    }

    public void run(Context context, Drive drive){
        drive.motor4.setIntakeMotorPower(1);
        motor.setMotorPower(1);
        context.waitForSeconds(5);
        motor.setMotorPower(0);
        drive.motor4.setIntakeMotorPower(0);
    }
}
