package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva_2025.constants.EncoderUtils;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    private MotorController elevatorLeftMotor;
    private MotorController elevatorRightMotor;
    private static final double MIN_HEIGHT = 0;
    private static final double MAX_HEIGHT = 150; // inches
    private static final double NUDGE_AMOUNT = EncoderUtils.elevatorHeightToRotations(5);
    private static final double THRESHOLD_CONSTANT =
            EncoderUtils.elevatorHeightToRotations(0); // TODO: Update me after testing!

    // values are untested and are set to

    public record ElevatorStatus(double currentHeight) implements Status {
        public boolean isAtPosition(double target) {
            return (Math.abs(target - currentHeight) < THRESHOLD_CONSTANT);
        }
    }

    public enum Position {
        ELEVATOR_TOP(MAX_HEIGHT),
        ELEVATOR_BOTTOM(MIN_HEIGHT);

        private double height;

        Position(double height) {
            this.height = height;
        }

        public double getHeight() {
            return height;
        }

        public double getElevatorRotations() {
            return EncoderUtils.elevatorHeightToRotations(height);
        }
    }

    public Elevator() {
        elevatorLeftMotor = RobotProvider.instance.getMotor("elevator.leftMotor");
        elevatorRightMotor = RobotProvider.instance.getMotor("elevator.rightMotor");
        elevatorRightMotor.follow(elevatorLeftMotor);
        elevatorLeftMotor.setSensorPosition(0);
    }

    /**
     *
     * @param setPosition in encoder units
     */
    public void setPosition(double setPosition) {
        if (setPosition >= MIN_HEIGHT && setPosition <= MAX_HEIGHT) {
            elevatorLeftMotor.set(MotorController.ControlMode.Position, setPosition);
        }
    }

    public void setPosition(Position position) {
        setPosition(position.getElevatorRotations());
    }

    public void nudgeUp() {
        double nudgePosition = elevatorLeftMotor.getSensorPosition() + NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void nudgeDown() {
        double nudgePosition = elevatorLeftMotor.getSensorPosition() - NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(
                EncoderUtils.elevatorRotationsToHeight(elevatorLeftMotor.getSensorPosition()));
    }
}
