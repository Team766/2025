package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class IntakeCoralUntilIn extends Procedure {
    private final CoralIntake intake;

    public IntakeCoralUntilIn(CoralIntake intake) {
        this.intake = reserve(intake);
    }

    @Override
    public void run(Context context) {
        intake.in();
        waitForStatusMatchingOrTimeout(
                context, CoralIntake.CoralIntakeStatus.class, s -> s.coralIntakeSuccessful(), 2);
        intake.idle();
    }
}
