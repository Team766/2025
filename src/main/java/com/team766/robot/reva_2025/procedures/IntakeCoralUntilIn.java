package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class IntakeCoralUntilIn extends Procedure {
    private static final double INTAKE_CURRENT_THRESHOLD = 8;
    private final CoralIntake intake;

    public IntakeCoralUntilIn(CoralIntake intake) {
        this.intake = reserve(intake);
    }

    @Override
    public void run(Context context) {
        intake.in();
        waitForStatusMatching(
                context,
                CoralIntake.CoralIntakeStatus.class,
                s -> s.current() > INTAKE_CURRENT_THRESHOLD);
        intake.stop();
    }
}
