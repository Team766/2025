package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;

public class IntakeAlgaeFromReef extends Procedure {
    private final AlgaeIntake intake;
    private final AlgaeIntake.Level targetLevel;

    public IntakeAlgaeFromReef(AlgaeIntake intake, AlgaeIntake.Level targetLevel) {
        this.intake = reserve(intake);
        this.targetLevel = targetLevel;
    }

    @Override
    public void run(Context context) {
        // move the intake arm to target level
        intake.setArmAngle(targetLevel);
        // start the intake
        intake.setState(State.In);
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus, s -> s.intakeProximity()<0.8);
        context.waitForSeconds(2);
        intake.setState(State.holdAlgae);
        // TODO keep intaking until prox sensor detects the algae at the desired position
        // move the arm down to either stow or shooter position
        intake.setArmAngle(Level.Shoot);
    }
}
