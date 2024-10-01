package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;

public class IntakeAlgaeFromReef extends Procedure {
    private final AlgaeIntake intake;
    private final AlgaeIntake.Level targetLevel;

    public IntakeAlgaeFromReef(AlgaeIntake intake, AlgaeIntake.Level targetLevel) {
        this.intake = reserve(intake);
        this.targetLevel = targetLevel;
    }

    @Override
    public void run(Context context) {
        // in BoxOp code we are calling IntakeAlgae separately from HoldAlgae, not using this
        // procedure
        // leaving this around for later
        context.runSync(new IntakeAlgae(intake, targetLevel));
        context.runSync(new HoldAlgae(intake));
    }
}
