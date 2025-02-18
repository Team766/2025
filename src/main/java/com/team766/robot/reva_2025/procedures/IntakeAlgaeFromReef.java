package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;

public class IntakeAlgaeFromReef extends Procedure {
    private final AlgaeIntake intake;
    private final AlgaeIntake.Level targetLevel;

    public IntakeAlgaeFromReef(AlgaeIntake intake, AlgaeIntake.Level targetLevel) {
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
        // context.waitForSeconds(2);
        intake.setArmAngle(Level.Shoot);
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle());
        intake.setState(State.HoldAlgae);
    }
}
