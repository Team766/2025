package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    private MotorController elevatorLeftMotor;
    private MotorController elevatorRightMotor;
    private static final double NUDGE_AMOUNT = 5;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private double setPoint;
    private final double thresholdConstant = 0; // TODO: Update me after testing!

    // values are untested and are set to

    public static record ElevatorStatus(double currentHeight, double targetHeight)
            implements Status {
        public boolean isAtHeight() {
            return Math.abs(targetHeight() - currentHeight()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum Position {
        ELEVATOR_TOP(22),
        ELEVATOR_BOTTOM(0);

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
        elevatorLeftMotor = RobotProvider.instance.getMotor(ConfigConstants.LEFT_ELEVATOR_MOTOR);
        elevatorRightMotor = RobotProvider.instance.getMotor(ConfigConstants.RIGHT_ELEVATOR_MOTOR);
        elevatorRightMotor.follow(elevatorLeftMotor);
        elevatorLeftMotor.setSensorPosition(0);
        setPoint = 0;
    }

    public void setPosition(double setPosition) {
        setPoint =
                com.team766.math.Math.clamp(
                        setPosition,
                        Position.ELEVATOR_BOTTOM.getHeight(),
                        Position.ELEVATOR_TOP.getHeight());
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
    protected void run() {
        elevatorLeftMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.elevatorHeightToRotations(setPoint));
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(
                EncoderUtils.elevatorRotationsToHeight(elevatorLeftMotor.getSensorPosition()),
                setPoint);
    }
}
