package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.InstantProcedure;
import com.team766.robot.jackrabbit.mechanisms.Feeder;
import com.team766.robot.jackrabbit.mechanisms.Shooter;
import com.team766.robot.jackrabbit.mechanisms.Spindexer;

public class Unshoot extends InstantProcedure {
    private final Spindexer spindexer;
    private final Feeder feeder;
    private final Shooter shooter;

    public Unshoot(Spindexer spindexer, Feeder feeder, Shooter shooter) {
        this.spindexer = reserve(spindexer);
        this.feeder = reserve(feeder);
        this.shooter = reserve(shooter);
    }

    @Override
    public void run() {
        spindexer.reverse();
        feeder.reverse();
        shooter.reverse();
    }
}
