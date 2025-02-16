package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;

public class ShootWhenReady extends Procedure {
    private AlgaeIntake algaeIntake;

    public ShootWhenReady(AlgaeIntake algaeIntake) {
        algaeIntake = reserve(algaeIntake);
    }

    public void run(Context context) {
        waitForStatusMatchingOrTimeout(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtTargetSpeed(), 1);
        algaeIntake.setState(State.Feed);
    }
}
