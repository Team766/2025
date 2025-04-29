package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;

public class RunCoralOut extends Procedure {

    private CoralIntake coral;
    private double seconds;

    public RunCoralOut(CoralIntake coral, double seconds) {
        this.coral = reserve(coral);
        this.seconds = seconds;
    }

    @Override
    public void run(Context context) {
        context.yield();
        coral.out();
        context.waitForSeconds(seconds);
        coral.stop();
    }
}
