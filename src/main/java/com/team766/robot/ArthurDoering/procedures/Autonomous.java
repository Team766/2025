package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;
import com.team766.robot.ArthurDoering.mechanisms.Shooter;

public class Autonomous extends Procedure {
    private Drive drive;
    private Shooter shooter;

    public void forward(Autonomous myDrive) {
        drive = reserve(myDrive);
    }

    public void shoot(Autonomous myShooter) {
        shooter = reserve(myShooter);
    }

    public void run(Context context){
        drive.move_left(1);
        drive.move_right(1);
        context.waitForSeconds(5);
        drive.move_left(0);
        drive.move_right(0);
        shooter.SetShooterSpeed(1);
        context.waitForSeconds(1);
        shooter.SetShooterSpeed(0);
    }
}
