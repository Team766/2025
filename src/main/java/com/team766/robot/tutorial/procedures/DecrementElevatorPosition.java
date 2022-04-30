package com.team766.robot.tutorial.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.tutorial.mechanisms.Elevator;

public class DecrementElevatorPosition extends Procedure {
    private final Elevator elevator;

    public DecrementElevatorPosition(Elevator elevator) {
        this.elevator = reserve(elevator);
    }

    @Override
    public void run(Context context) {
        elevator.setSetpoint(getStatusOrThrow(Elevator.ElevatorStatus.class).setpoint() - 6);
    }
}
