package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;

public class Autonomous extends Procedure{
    private Drive drive;
    private Shooter shooter;

    public forward(Autonomous myDrive){
        drive = reserve(myDrive);
    }

    public shoot(Autonomous myShooter){
        shooter = reserve(myShooter);
    }

    public void run(Context context){
        drive.setMotorPower(1);
        context.waitForSeconds(5);
        drive.setMotorPower(0);
        shooter.setMotorPower(1);
        context.waitForSeconds(1);
        shooter.setMotorPower(0);
    }
}
