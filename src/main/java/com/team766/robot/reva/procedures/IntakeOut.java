package com.team766.robot.reva.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva.mechanisms.Intake;

public class IntakeOut extends Procedure {
    private final Intake intake;

    public IntakeOut(Intake intake) {
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.out();
    }
}
