package com.team766.robot.copy_2910.mechanisms;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
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
    // private ValueProvider<Double> ffGain;
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

        elevatorMotorRight.follow(elevatorMotorLeft);
        setPoint = ElevatorPosition.READY.getPosition(); // Default position
        elevatorMotorLeft.setCurrentLimit(
                35); // Set current limit for the elevator motor | TODO: Replace with actual value
        elevatorMotorLeft.setInverted(false);
        SparkMaxConfig rightConfig = new SparkMaxConfig();
        rightConfig.follow((SparkMax)elevatorMotorLeft, true /* invert */);
        ((SparkMax)elevatorMotorRight).configure(
            rightConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);
        // ffGain =
        //      ConfigFileReader.instance.getDouble(
        //            "ElevatorFFGain"); // Replace with actual config key

        elevatorMotorLeft.setSensorPosition(
                0.0); // Elevator always has to start at same 0.0 position
        //elevatorMotorRight.setInverted(true);
    }

    public enum ElevatorPosition {
        INTAKE(0.0),
        L1(0.881),
        L2(-4.452),
        L3(-11.5),
        L4(-23),  //-21.357
        ALGAE_HIGH(-9.357),
        ALGAE_LOW(-3.262),
        CORAL_GROUND(-0.357),
        ALGAE_GROUND(-1.643),
        READY(-10), // Should be the default position and the ready position for vision so that it
        // can see the tag
        MAXIMUM(2), // Maximum height of the elevator, TODO: Adjust based on the actual elevator's
        // maximum position
        MINIMUM(-25); // Minimum height of the elevator, TODO: Adjust based on the actual elevator's
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
        elevatorMotorLeft.set(MotorController.ControlMode.Position, setPoint);
        log("SHOULDER Setpoint: " + setPoint + " Pos: " + elevatorMotorLeft.getSensorPosition());
    }

    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(elevatorMotorLeft.getSensorPosition(), setPoint);
    }
}
