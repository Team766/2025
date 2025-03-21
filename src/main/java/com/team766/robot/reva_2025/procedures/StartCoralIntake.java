package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class StartCoralIntake extends Procedure {
    private CoralIntake coralIntake;

    public StartCoralIntake(CoralIntake coralIntake) {
        this.coralIntake = reserve(coralIntake);
    }

    public void run(Context context) {
        coralIntake.in();
    }
}
