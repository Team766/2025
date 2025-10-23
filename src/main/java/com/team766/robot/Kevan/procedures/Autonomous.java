package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Shooter;

public class Autonomous extends Procedure {
    private Drive drive;
    private Shooter shooter;
    
    public Autonomous(Drive myDrive, Shooter myShooter) {
        drive = reserve(myDrive);
        shooter = reserve(myShooter);
    }

    public void AutonProc(Context context) {
        drive.move_straight(1);
        context.waitForSeconds(4);
        drive.move_straight(0);
        shooter.move(0.5);
        context.waitForSeconds(2);
        shooter.move(0);

    }
    
}