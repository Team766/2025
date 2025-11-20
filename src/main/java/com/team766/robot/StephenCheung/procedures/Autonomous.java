package com.team766.robot.StephenCheung.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.StephenCheung.mechanisms.Drive;
import com.team766.robot.StephenCheung.mechanisms.Intake;
import com.team766.robot.StephenCheung.mechanisms.Shooter;

public class Autonomous extends Procedure {
    private Drive drive;
    private Intake intake;
    private Shooter shooter;

    public Autonomous(Drive drive, Shooter shooter, Intake intake) {
        this.drive = reserve(drive);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        context.runSync(new DriveProcedure(drive, 1));
        context.runSync(new ShooterProcedure(shooter, 1));
        context.runSync(new TurningProcedure(drive, 1, 1));
        context.runSync(new ShooterProcedure(shooter, 1));
    }
}
