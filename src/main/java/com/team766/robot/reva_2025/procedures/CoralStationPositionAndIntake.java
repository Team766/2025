package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;

import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;

public class CoralStationPositionAndIntake extends Procedure {
    private AlgaeIntake algaeIntake;
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coralIntake;

    public CoralStationPositionAndIntake(AlgaeIntake algaeIntake, Elevator elevator, Wrist wrist, CoralIntake coralIntake) {
        this.algaeIntake = reserve(algaeIntake);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
    }

    public void run(Context context) {
        algaeIntake.setArmAngle(Level.Stow);
        elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE);
        wrist.setAngle(WristPosition.CORAL_INTAKE);
        
        waitForStatusMatchingOrTimeout(
                context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle(), 1);
        waitForStatusMatchingOrTimeout(
                context, Elevator.ElevatorStatus.class, s -> s.isAtHeight(), 0.5);
        waitForStatusMatchingOrTimeout(context, Wrist.WristStatus.class, s -> s.isAtAngle(), 0.5);

        context.runSync(new IntakeCoralUntilIn(coralIntake));
    }
}
