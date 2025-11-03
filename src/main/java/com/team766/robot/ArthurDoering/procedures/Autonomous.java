package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;




public class Autonomous extends Procedure {
    private Autonomous drive;
    private Autonomous shooter;
    private Autonomous intake;

    public Autonomous(Autonomous myDrive, Autonomous myShooter, Autonomous myIntake) {
        this.drive = reserve(myDrive);
        this.shooter = reserve(myShooter);
        this.intake = reserve(myIntake);
    }

    public void run(Context context){
        context.waitForSeconds(1);
        context.runParallel(new DriveProcedure(drive, 1.25));
        context.runParallel(new ShootProcedure(shooter, 0.8));
        context.runParallel(new TurnProcedure(drive, 0.25, 0.5));
        context.runParallel(new DriveProcedure(drive, 0.3125));
        context.runParallel(new IntakeProcedure(intake));
        context.runParallel(new DriveProcedure(drive,0.3125));
        context.runParallel(new IntakeProcedure(intake));
        context.runParallel(new TurnProcedure(drive, 0.5, 0.5));
        context.runParallel(new DriveProcedure(drive, 0.625));
        context.runParallel(new TurnProcedure(drive, 0.25, 0.5));
        context.runParallel(new ShootProcedure(shooter, 0.8));
        context.runParallel(new ShootProcedure(shooter, 0.8));
    }
}
