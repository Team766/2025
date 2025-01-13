package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;

public class IntakeStop extends Procedure {
    private final Intake intake;

    public IntakeStop(Intake intake) {
        this.intake = intake;
    }

    public void run(Context context) {
        intake.stop();
    }
}
