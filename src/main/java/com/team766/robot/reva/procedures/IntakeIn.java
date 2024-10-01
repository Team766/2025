package com.team766.robot.reva.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva.mechanisms.Intake;

public class IntakeIn extends Procedure {
    private final Intake intake;

    public IntakeIn(Intake intake) {
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.in();
    }
}
