package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Intake;

public class IntakeProcedure extends Procedure {

    private Intake intake;

    public IntakeProcedure(Intake myIntake) {
        intake = reserve(myIntake);
    }

    public void run(Context context) {
        intake.SetIntake(1);
        context.waitForSeconds(1);
        intake.SetIntake(0);
    }
}
