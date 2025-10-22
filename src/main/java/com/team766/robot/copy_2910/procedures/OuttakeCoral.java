package com.team766.robot.copy_2910.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.copy_2910.mechanisms.Intake;

public class OuttakeCoral extends Procedure {

    private Intake intake;

    public OuttakeCoral(Intake intake2) {
        intake = reserve(intake2);
    }

    @Override
    public void run(Context context) {
        intake.setLeft(1.5);
        intake.setRight(-1.5
        );
        waitForStatusMatching(context, Intake.IntakeStatus.class, s -> !s.hasCoralToOuttake());
        intake.stop();
    }
}
