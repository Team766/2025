package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Intake;

public class IntakeProcedure extends Procedure {

    private Intake intake;

    public IntakeProcedure(Intake myIntake) {
        intake = reserve(myIntake);
    }

    public void run(Context context) {
        intake.setIntake(0.75);
        context.waitForSeconds(1);
        intake.setIntake(0);
    }
}
