package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    private MotorController elevatorLeftMotor;
    private MotorController elevatorRightMotor;
    private static final double MIN_HEIGHT = 0;
    private static final double MAX_HEIGHT = 150;
    private static final double L1_HEIGHT = 1; // TEMPORARY VALUE
    private static final double L2_HEIGHT = 2; // TEMPORARY VALUE
    private static final double L3_HEIGHT = 3; // TEMPORARY VALUE
    private static final double L4_HEIGHT = 4; // TEMPORARY VALUE
    private static final double NUDGE_AMOUNT = 5;
    private double setPoint;
    private final double thresholdConstant = 0; // TODO: Update me after testing!

    // values are untested and are set to

    public static record ElevatorStatus(double currentHeight) implements Status {}

    public enum Position {
        ELEVATOR_TOP(EncoderUtils.elevatorRotationsToHeight(MAX_HEIGHT)),
        ELEVATOR_BOTTOM(EncoderUtils.elevatorRotationsToHeight(MIN_HEIGHT)),

        ELEVATOR_L1(EncoderUtils.elevatorRotationsToHeight(L1_HEIGHT)),
        ELEVATOR_L2(EncoderUtils.elevatorRotationsToHeight(L2_HEIGHT)),
        ELEVATOR_L3(EncoderUtils.elevatorRotationsToHeight(L3_HEIGHT)),
        ELEVATOR_L4(EncoderUtils.elevatorRotationsToHeight(L4_HEIGHT));

        double height = 0;

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
        setPoint = 0;
    }

    public void setPosition(double setPosition) {
        if (setPosition >= MIN_HEIGHT && setPosition <= MAX_HEIGHT) {
            setPoint = setPosition;
            elevatorLeftMotor.set(MotorController.ControlMode.Position, setPosition);
        }
    }

    public void setPosition(Position position) {
        setPosition(position.getElevatorRotations());
    }

    public void nudge(double sign) {
        double nudgePosition = elevatorLeftMotor.getSensorPosition() + (NUDGE_AMOUNT * Math.signum(sign));
        setPosition(nudgePosition);
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(
                EncoderUtils.elevatorRotationsToHeight(elevatorLeftMotor.getSensorPosition()));
    }

    public boolean isAtPosition() {
        return (Math.abs(setPoint - elevatorLeftMotor.getSensorPosition()) < thresholdConstant);
    }
}
