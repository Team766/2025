package com.team766.robot.copy_2910.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.math.Maths;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {

    private MotorController elevatorMotorRight;
    private MotorController elevatorMotorLeft;

    private static double NUDGE_AMOUNT =
            0.1; // Amount to nudge up/down | TODO: Adjust this value based on the elevator's
    // characteristics
    private static double THRESHOLD =
            0.05; // Threshold for PID controller | TODO: Adjust this value based on the elevator's
    // characteristics
    private ValueProvider<Double> ffGain;
    private double setPoint;

    public static record ElevatorStatus(double currentPosition, double targetPosition)
            implements Status {
        public boolean isAtHeight() {
            return Math.abs(currentPosition - targetPosition) < THRESHOLD;
        }
    }

    public Elevator() {
        elevatorMotorRight =
                RobotProvider.instance.getMotor(
                        "ElevatorMotorRight"); // Replace with actual motor name
        elevatorMotorLeft =
                RobotProvider.instance.getMotor(
                        "ElevatorMotorLeft"); // Replace with actual motor name

        // TODO: FIGURE OUT WHICH MOTOR NEEDS TO BE INVERTED
        // elevatorMotorRight.setInverted(false); // Set to true if the right motor needs to be
        // inverted

        elevatorMotorLeft.follow(elevatorMotorRight);
        setPoint = ElevatorPosition.READY.getPosition(); // Default position
        elevatorMotorRight.setCurrentLimit(
                30); // Set current limit for the elevator motor | TODO: Replace with actual value
        ffGain =
                ConfigFileReader.instance.getDouble(
                        "ElevatorFFGain"); // Replace with actual config key

        elevatorMotorRight.setSensorPosition(
                0.0); // Elevator always has to start at same 0.0 position
    }

    public enum ElevatorPosition {
        INTAKE(0.0),
        CORAL_L1(0.5),
        CORAL_L2(1.0),
        CORAL_L3(1.5),
        CORAL_L4(2.0),
        READY(0.4), // Should be the default position and the ready position for vision so that it
        // can see the tag
        MAXIMUM(2.5), // Maximum height of the elevator, TODO: Adjust based on the actual elevator's
        // maximum position
        MINIMUM(0.0); // Minimum height of the elevator, TODO: Adjust based on the actual elevator's
        // minimum position

        final double position;

        ElevatorPosition(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    public void setPosition(double setPosition) {
        setPoint =
                Maths.clamp(
                        setPosition,
                        ElevatorPosition.MINIMUM.getPosition(),
                        ElevatorPosition.MAXIMUM.getPosition());
    }

    public void nudgeUp() {
        setPosition(setPoint + NUDGE_AMOUNT);
    }

    public void nudgeDown() {
        setPosition(setPoint - NUDGE_AMOUNT);
    }

    public void run() {
        elevatorMotorRight.set(MotorController.ControlMode.Position, setPoint, ffGain.valueOr(0.0));
    }

    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(elevatorMotorRight.getSensorPosition(), setPoint);
    }
}
