package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Shooter;
import com.team766.robot.Kevan.mechanisms.Intake;

public class Autonomous extends Procedure {
    private Drive drive;
    private Shooter shooter;
    private Intake intake;

    public Autonomous(Drive myDrive, Shooter myShooter, Intake myIntake) {
        drive = reserve(myDrive);
        shooter = reserve(myShooter);
        intake = reserve(myIntake);
    }

    public void run(Context context) {
        drive.move_straight(1);
        context.waitForSeconds(2);
        drive.move_straight(0);
        shooter.SetShooterSpeed(0.5);
        context.waitForSeconds(2);
        shooter.SetShooterSpeed(0);
    }
}
