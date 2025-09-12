package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.InstantProcedure;
import com.team766.robot.jackrabbit.mechanisms.Collector;
import com.team766.robot.jackrabbit.mechanisms.Feeder;
import com.team766.robot.jackrabbit.mechanisms.Spindexer;

public class Intake extends InstantProcedure {

    private final Collector collector;
    private final Spindexer spindexer;
    private final Feeder feeder;

    public Intake(Collector collector, Spindexer spindexer, Feeder feeder) {
        this.collector = reserve(collector);
        this.spindexer = reserve(spindexer);
        this.feeder = reserve(feeder);
    }

    @Override
    public void run() {
        collector.intake();
        spindexer.move();
        feeder.index();
    }
}
