package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;

public class CoralStationPositionAndIntake extends Procedure {
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coralIntake;

    public CoralStationPositionAndIntake(Elevator elevator, Wrist wrist, CoralIntake coralIntake) {
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coralIntake = reserve(coralIntake);
    }

    public void run(Context context) {
        elevator.setPosition(ElevatorPosition.ELEVATOR_INTAKE);
        wrist.setAngle(WristPosition.CORAL_INTAKE);
        coralIntake.in();

        waitForStatusMatchingOrTimeout(
                context, Elevator.ElevatorStatus.class, s -> s.isAtHeight(), 1.0);
        waitForStatusMatchingOrTimeout(context, Wrist.WristStatus.class, s -> s.isAtAngle(), 0.5);
        waitForStatusMatchingOrTimeout(
                context,
                CoralIntake.CoralIntakeStatus.class,
                s -> s.current() > IntakeCoralUntilIn.INTAKE_CURRENT_THRESHOLD, 2);

        coralIntake.idle();
    }
}
