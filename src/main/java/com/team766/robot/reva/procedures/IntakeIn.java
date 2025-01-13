package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;

public class IntakeIn extends Procedure {
    private final Intake intake;

    public IntakeIn(Intake intake) {
        this.intake = intake;
    }

    public void run(Context context) {
        intake.in();
    }
}
