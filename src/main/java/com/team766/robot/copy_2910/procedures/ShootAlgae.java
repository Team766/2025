package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Intake;

public class ShootAlgae extends Procedure {

    private Intake intake;

    public ShootAlgae(Intake intake2) {
        intake = reserve(intake2);
    }

    @Override
    public void run(Context context) {
        context.waitForSeconds(.1);
        intake.turnAlgaeNegative();
    }
}
