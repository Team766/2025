package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.logging.Category;
import com.team766.robot.tutorial.mechanisms.Drive;
import com.team766.robot.tutorial.mechanisms.Intake;
import com.team766.robot.tutorial.mechanisms.Launcher;

public class ExampleDriveSequence extends Procedure {
    private final Drive drive;
    private final Intake intake;
    private final Launcher launcher;

    public ExampleDriveSequence(Drive drive, Intake intake, Launcher launcher) {
        this.drive = reserve(drive);
        this.intake = reserve(intake);
        this.launcher = reserve(launcher);
    }

    public void run(Context context) {
        intake.setIntakeArm(true);
        intake.setIntakePower(1.0);

        for (int i = 0; i < 4; ++i) {
            log("Forward movement begins");
            drive.setDrivePower(0.3, 0.3);
            context.waitForSeconds(2.3);
            log("Forward movement finished");

            log("Turning movement begins");
            drive.setDrivePower(0.1, -0.1);
            context.waitForSeconds(2.35);
            log("Turning movement finished");

            context.runSync(new Launch(launcher));
        }

        drive.setDrivePower(0.0, 0.0);
    }

    @Override
    public Category getLoggerCategory() {
        return Category.AUTONOMOUS;
    }
}
