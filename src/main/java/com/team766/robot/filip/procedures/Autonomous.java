package com.team766.robot.filip.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.filip.mechanisms.Drive;
import com.team766.robot.filip.mechanisms.FilipMovingMotor;

public class Autonomous extends Procedure {
    private FilipMovingMotor motor;
    private Drive drive;
    public Autonomous(Drive myDrive, FilipMovingMotor myMotor){
        drive = reserve(myDrive);
        motor = reserve(myMotor);
    }
    public void run(Context context) {
        drive.move_left(1);
        drive.move_right(1);

        context.waitForSeconds(5);

        drive.move_left(0);
        drive.move_right(0);

        motor.moveSpeed(1);
        context.waitForSeconds(3);
        motor.moveSpeed(0);
    }
    
}
