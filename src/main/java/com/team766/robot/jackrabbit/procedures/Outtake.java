package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.InstantProcedure;
import com.team766.robot.jackrabbit.mechanisms.Collector;

public class Outtake extends InstantProcedure {

    private final Collector collector;

    public Outtake(Collector collector) {
        this.collector = reserve(collector);
    }

    @Override
    public void run() {
        collector.outtake();
    }
}
