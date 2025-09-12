package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.InstantProcedure;
import com.team766.robot.jackrabbit.mechanisms.Feeder;
import com.team766.robot.jackrabbit.mechanisms.Spindexer;

public class Shoot extends InstantProcedure {
    private final Spindexer spindexer;
    private final Feeder feeder;

    public Shoot(Spindexer spindexer, Feeder feeder) {
        this.spindexer = reserve(spindexer);
        this.feeder = reserve(feeder);
    }

    @Override
    public void run() {
        spindexer.move();
        feeder.feed();
    }
}
