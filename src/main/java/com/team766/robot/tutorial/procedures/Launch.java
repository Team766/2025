package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Launcher;

public class Launch extends Procedure {
    private final Launcher launcher;

    public Launch(Launcher launcher) {
        this.launcher = reserve(launcher);
    }

    public void run(Context context) {
        log("Launching ball");
        launcher.setPlunger(true);
        context.waitForSeconds(0.5);
        launcher.setPlunger(false);
    }
}
