package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.jackrabbit.mechanisms.Turret;

public class InitializeTurret extends Procedure {
    private final Turret turret;

    public InitializeTurret(Turret turret) {
        this.turret = reserve(turret);
    }

    @Override
    public void run(Context context) {
        turret.moveCCWForInitialization();
        context.yield(); // Allow uninitialized state to propagate
        waitForStatusMatching(context, Turret.TurretStatus.class, s -> s.initialized());
        turret.stop();
    }
}
