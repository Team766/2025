package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Intake;
import java.util.Optional;

public class OuttakeCoral extends Procedure {

    private Intake intake;

    public OuttakeCoral() {
        intake = reserve(intake);
    }

    @Override
    public void run(Context context) {
        intake.turnRightPositive();
        intake.turnLeftNegative();
        intake.turnAlgaeNegative();
        waitForStatusMatching(context, Intake.IntakeStatus.class, s -> s.hasCoralInFrontCenter());
        intake.stop();
    }
}
