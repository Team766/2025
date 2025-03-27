package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;

public class HoldAlgae extends Procedure {
    private final AlgaeIntake intake;

    public HoldAlgae(AlgaeIntake intake) {
        this.intake = reserve(intake);
    }

    @Override
    public void run(Context context) {
        // context.waitForSeconds(2);
        intake.setArmAngle(Level.Shoot);
        intake.setState(State.MatchVelocity);
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle());
        intake.setState(State.HoldAlgae);
    }
}
