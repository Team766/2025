package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.mechanisms.Shooter;

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
        context.runParallel(new DriveProcedure(drive, 1));
        context.runParallel(new ShootProcedure(shooter, 1));
        context.runParallel(new TurnProcedure(drive, 0.25, 0.5));
        context.runParallel(new DriveProcedure(drive, 0.25));
        context.runParallel(new IntakeProcedure(intake));
        context.runParallel(new DriveProcedure(drive, 0.25));
        context.runParallel(new IntakeProcedure(intake));
        context.runParallel(new TurnProcedure(drive, 0.5, 0.5));
        context.runParallel(new DriveProcedure(drive, 0.5));
        context.runParallel(new TurnProcedure(drive, 0.25, 0.5));
        context.runParallel(new ShootProcedure(shooter, 1));
        context.runParallel(new ShootProcedure(shooter, 1));
    }
}
