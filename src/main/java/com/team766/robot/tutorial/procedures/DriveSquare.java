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
        // This loop repeats 4 times.
        for (int i = 0; i < 4; ++i) {
            // Drive along the side of the square
            context.runSync(new DriveStraight(drive));

            // Turn at the corner
            context.runSync(new TurnRight(drive));
        }
    }
}
