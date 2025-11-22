package com.team766.robot.filip.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.filip.mechanisms.Drive;
import com.team766.robot.filip.mechanisms.Intake;
import com.team766.robot.filip.mechanisms.Shooter;

public class Autonomous extends Procedure {
    private Drive drive;
    private Shooter shooter;
    private Intake intake;

    public Autonomous(Drive myDrive, Shooter myShooter, Intake myIntake) {
        this.drive = reserve(myDrive);
        this.shooter = reserve(myShooter);
        this.intake = reserve(myIntake);
    }

    public void run(Context context) {
        context.runSync(new DriveProcedure(drive, 1.25));
        context.runSync(new ShootProcedure(shooter, 0.8));
        context.runSync(new TurnProcedure(drive, 0.25, 0.5));
        context.runSync(new DriveProcedure(drive, 0.3125));
        context.runSync(new IntakeProcedure(intake));
        shooter.SetTransferSpeed(1);
        context.waitForSeconds(0.5);
        shooter.SetTransferSpeed(0);
        context.runSync(new DriveProcedure(drive, 0.3125));
        context.runSync(new IntakeProcedure(intake));
        context.runSync(new TurnProcedure(drive, 0.5, 0.5));
        context.runSync(new DriveProcedure(drive, 0.625));
        context.runSync(new TurnProcedure(drive, 0.25, 0.5));
        shooter.SetShooterSpeed(0.8);
        context.waitForSeconds(0.25);
        shooter.SetShooterSpeed(0);
        context.runSync(new ShootProcedure(shooter, 0.8));
    }
}
