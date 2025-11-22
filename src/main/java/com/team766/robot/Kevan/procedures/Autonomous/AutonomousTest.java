package com.team766.robot.Kevan.procedures.Autonomous;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;
import com.team766.robot.Kevan.mechanisms.Intake;
import com.team766.robot.Kevan.mechanisms.Shooter;
import com.team766.robot.Kevan.procedures.DriveProcedure;
import com.team766.robot.Kevan.procedures.IntakeProcedure;
import com.team766.robot.Kevan.procedures.ShootProcedure;
import com.team766.robot.Kevan.procedures.TurnProcedure;

public class AutonomousTest extends Procedure {
    private Drive drive;
    private Shooter shooter;
    private Intake intake;

    public AutonomousTest(Drive myDrive, Shooter myShooter, Intake myIntake) {
        this.drive = reserve(myDrive);
        this.shooter = reserve(myShooter);
        this.intake = reserve(myIntake);
    }

    public void run(Context context) {
        context.runSync(new DriveProcedure(drive, 0.5));
        //context.runSync(new ShootProcedure(shooter, 1));
        //context.runSync(new IntakeProcedure(intake));
        //context.runSync(new TurnProcedure(drive, 1));
    }
}
