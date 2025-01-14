package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shoulder;

public class StartAutoIntake extends Procedure {
    private final Shoulder shoulder;
    private final Intake intake;

    public StartAutoIntake(Shoulder shoulder, Intake intake) {
        this.shoulder = reserve(shoulder);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        final var armTarget = Shoulder.ShoulderPosition.BOTTOM;
        shoulder.rotate(armTarget);
        waitForStatusMatchingOrTimeout(
                context, Shoulder.ShoulderStatus.class, s -> s.isNearTo(armTarget), 1.5);
        intake.setIntakePowerFromSensorDistance();
    }
}
