package com.team766.robot.StephenCheung.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.StephenCheung.mechanisms.Intake;

public class IntakeProcedure extends Procedure {

    private Intake intake;

    public IntakeProcedure(Intake intake) {
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.SetIntake(1);
        context.waitForSeconds(0.5);
        intake.SetIntake(0);
    }
}
