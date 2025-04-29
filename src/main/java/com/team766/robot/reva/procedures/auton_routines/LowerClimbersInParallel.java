package com.team766.robot.reva.procedures.auton_routines;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva.mechanisms.Climber;
import com.team766.robot.reva.procedures.MoveClimbersToBottom;

public class LowerClimbersInParallel extends Procedure {
    private final Procedure autonomousRoutine;
    private final Climber climber;

    public LowerClimbersInParallel(Procedure autonomousRoutine, Climber climber) {
        this.autonomousRoutine = autonomousRoutine;
        reserve(autonomousRoutine.reservations());
        this.climber = reserve(climber);
    }

    @Override
    public void run(Context context) {
        context.runParallel(new MoveClimbersToBottom(climber), autonomousRoutine);
    }
}
