package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.logging.Category;
import com.team766.robot.tutorial.mechanisms.Drive;

public class DriveSquare extends Procedure {

    private final Drive drive;

    public DriveSquare(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        // First side
        context.runSync(new DriveStraight(drive));
        log("First side complete");

        // First corner
        context.runSync(new TurnRight(drive));
        log("First corner complete");

        // Second side
        context.runSync(new DriveStraight(drive));
        log("Second side complete");

        // Second corner
        context.runSync(new TurnRight(drive));
        log("Second corner complete");

        // Third side
        context.runSync(new DriveStraight(drive));
        log("Third side complete");

        // Third corner
        context.runSync(new TurnRight(drive));
        log("Third corner complete");

        // Fourth side
        context.runSync(new DriveStraight(drive));
        log("Fourth side complete");

        // Fourth corner
        context.runSync(new TurnRight(drive));
        log("Fourth corner complete");
    }

    @Override
    public Category getLoggerCategory() {
        return Category.AUTONOMOUS;
    }
}
