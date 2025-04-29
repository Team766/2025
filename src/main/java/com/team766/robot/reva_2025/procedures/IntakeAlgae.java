package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;

public class IntakeAlgae extends Procedure {
    private final AlgaeIntake intake;
    private final AlgaeIntake.Level targetLevel;

    public IntakeAlgae(AlgaeIntake intake, AlgaeIntake.Level targetLevel) {
        this.intake = reserve(intake);
        this.targetLevel = targetLevel;
    }

    @Override
    public void run(Context context) {
        // move the intake arm to target level
        intake.setArmAngle(targetLevel);
        intake.setState(State.In);
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle());
        intake.setState(State.InUntilStable);
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAlgaeStable());
    }
}
