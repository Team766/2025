package com.team766.robot.Jordan.procedures;

import com.ctre.phoenix6.hardware.jni.HardwareJNI.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Jordan.mechanisms.MovingMotor;

public class MoveMotorProcedure extends Procedure{
    private MovingMotor motor;

    public void MoveMotorProcedure(MovingMotor myMotor) {
        motor = reserve(myMotor);
    }

    public void run(Context context){
        motor.setMotorPower(1);
        context.waitForSeconds(5);
        motor.setMotorPower(0);
    }
}
