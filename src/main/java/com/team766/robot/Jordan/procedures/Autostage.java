package com.team766.robot.Jordan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Jordan.mechanisms.Drive;
import com.team766.robot.Jordan.mechanisms.MovingMotor;

public class Autostage extends Procedure {

    private Drive drive;
    private MovingMotor motor;

    public void MoveMotor(Drive Motor1, MovingMotor Motor2){ 


        drive = reserve(Motor1);
        motor = reserve(Motor2);

    }
    
    @Override
    public void run(Context context){
        drive.setMotorPower(1, 0);
        context.waitForSeconds(7);
        drive.setMotorPower(0, 0);
        context.waitForSeconds(2);
        motor.setMotorPower(1);
        context.waitForSeconds(1);
        motor.setMotorPower(0);
    }    
}