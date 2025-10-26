package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;

public class Autonomous extends Procedure{
    private Drive drive;
    private Shooter shooter;

    public void forward(Autonomous myDrive){
        drive = reserve(myDrive);
    }

    public void shoot(Autonomous myShooter){
        shooter = reserve(myShooter);
    }

    public void run(Context context){
        drive.moveLeft(1);
        drive.moveRight(1);
        context.waitForSeconds(5);
        drive.moveLeft(0);
        drive.moveRight(0);
        shooter.setShooterSpeed(1);
        context.waitForSeconds(1);
        shooter.setShooterSpeed(0);
    }
}