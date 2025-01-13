package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Climber;

public class MoveClimbersToBottom extends Procedure {
    private final Climber climber;

    public MoveClimbersToBottom(Climber climber) {
        this.climber = reserve(climber);
    }

    public void run(Context context) {
        climber.setPower(0.25);
        context.waitFor(
                () ->
                        climber.getStatus().isLeftNear(Climber.ClimberPosition.BOTTOM)
                                && climber.getStatus().isRightNear(Climber.ClimberPosition.BOTTOM));
        climber.stop();
    }
}
