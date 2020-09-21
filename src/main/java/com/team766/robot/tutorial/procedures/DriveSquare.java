package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Drive;

public class DriveSquare extends Procedure {

    private final Drive drive;

    public DriveSquare(Drive drive) {
        this.drive = reserve(drive);
    }

    public void run(Context context) {
        // First side
        context.runSync(new DriveStraight(drive));

        // First corner
        context.runSync(new TurnRight(drive));

        // Second side
        context.runSync(new DriveStraight(drive));

        // Second corner
        context.runSync(new TurnRight(drive));

        // Third side
        context.runSync(new DriveStraight(drive));

        // Third corner
        context.runSync(new TurnRight(drive));

        // Fourth side
        context.runSync(new DriveStraight(drive));

        // Fourth corner
        context.runSync(new TurnRight(drive));
    }
}
