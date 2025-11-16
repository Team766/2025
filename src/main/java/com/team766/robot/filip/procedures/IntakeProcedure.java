package com.team766.robot.filip.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.filip.mechanisms.Intake;

public class IntakeProcedure extends Procedure {

    private Intake intake;

    public IntakeProcedure(Intake myIntake) {
        intake = reserve(myIntake);
    }

    public void run(Context context) {
        intake.SetIntake(1);
        context.waitForSeconds(0.5);
        intake.SetIntake(0);
    }
}
