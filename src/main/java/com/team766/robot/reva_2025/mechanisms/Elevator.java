package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    private MotorController elevatorLeftMotor;
    private MotorController elevatorRightMotor;
    private double currentPosition;
    private final double MIN_HEIGHT = 0;
    private final double MAX_HEIGHT = 150;
    private final double NUDGE_AMOUNT = 5;

    // values are untested and are set to

    public static record ElevatorStatus(double currentPosition) implements Status {}

    public Elevator() {
        elevatorLeftMotor = RobotProvider.instance.getMotor("elevator.leftMotor");
        elevatorRightMotor = RobotProvider.instance.getMotor("elevator.rightMotor");
        elevatorRightMotor.follow(elevatorLeftMotor);
        currentPosition = 0;
        elevatorLeftMotor.setSensorPosition(0);
    }

    public void setPosition(double setPosition) {
        if (setPosition >= MIN_HEIGHT && setPosition <= MAX_HEIGHT) {
            elevatorLeftMotor.set(MotorController.ControlMode.Position, setPosition);
            currentPosition = setPosition;
        }
    }

    public void nudgeUp() {
        double nudgePosition = elevatorLeftMotor.getSensorPosition() + NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void nudgeDown() {
        double nudgePosition = elevatorLeftMotor.getSensorPosition() - NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(currentPosition);
    }
}
