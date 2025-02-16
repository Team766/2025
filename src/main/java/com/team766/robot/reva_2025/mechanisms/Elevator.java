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

    public static record ElevatorStatus(double currentHeight, double targetHeight)
            implements Status {
        public boolean isAtHeight() {
            return Math.abs(targetHeight() - currentHeight()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum Position {
        ELEVATOR_TOP(22),
        ELEVATOR_BOTTOM(0),
        ELEVATOR_INTAKE(10),
        ELEVATOR_L1(0),
        ELEVATOR_L2(0),
        ELEVATOR_L3(0),
        ELEVATOR_L4(ELEVATOR_TOP.getHeight());

        double height = 0;

        Position(double height) {
            this.height = height;
        }

        public double getHeight() {
            return height;
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
        setPosition(position.getHeight());
    }

    public void nudge(double sign) {
        double nudgePosition =
                elevatorLeftMotor.getSensorPosition() + (NUDGE_AMOUNT * Math.signum(sign));
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
