package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Intake;

public class StopIntake extends Procedure {
    private final Intake intake;

    public StopIntake(Intake intake) {
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        intake.setWheelPower(0.0);
        intake.setExtended(false);
    }
}
