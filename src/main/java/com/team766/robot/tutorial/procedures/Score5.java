package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;
import com.team766.robot.tutorial.mechanisms.Intake;
import com.team766.robot.tutorial.mechanisms.Launcher;

public class Score5 extends Procedure {
    private final Drive drive;
    private final Launcher launcher;
    private final Intake intake;

    public Score5(Drive drive, Launcher launcher, Intake intake) {
        this.drive = reserve(drive);
        this.launcher = reserve(launcher);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        context.runSync(new CollectBalls(drive, intake));

        context.runSync(new DriveBackward(drive));

        context.runSync(new TurnRight(drive));

        drive.setDrivePower(0.25, 0.25);
        context.waitForSeconds(0.8);
        drive.setDrivePower(0.0, 0.0);
        context.waitForSeconds(0.5);

        for (int i = 0; i < 5; ++i) {
            context.runSync(new Launch(launcher));
        }
    }
}
