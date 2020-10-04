package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;
import com.team766.robot.tutorial.mechanisms.Intake;

public class CollectBalls extends Procedure {
    private final Drive drive;
    private final Intake intake;

    public CollectBalls(Drive drive, Intake intake) {
        this.drive = reserve(drive);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        context.runSync(new StartIntake(intake));

        context.runSync(new DriveStraight(drive));

        context.runSync(new StopIntake(intake));
    }
}
