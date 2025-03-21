package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;

public class MoveElevator extends Procedure {
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coralIntake;
    private ScoreHeight scoreHeight;

    public MoveElevator(
            Elevator elevator, Wrist wrist, CoralIntake coralIntake, ScoreHeight height) {
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coralIntake = reserve(coralIntake);
    }

    public void run(Context context) {
        elevator.setPosition(scoreHeight.getElevatorPosition());
        wrist.setAngle(scoreHeight.getWristPosition());
        if (scoreHeight == ScoreHeight.Intake) {
            coralIntake.in();
        }
    }
}
