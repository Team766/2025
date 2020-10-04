package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Intake;

public class StartIntake extends Procedure {
    private final Intake intake;

    public StartIntake(Intake intake) {
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.setWheelPower(1.0);
        intake.setExtended(true);
    }
}
