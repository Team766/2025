package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.*;
import com.team766.hal.wpilib.CANRangeTimeOfFlight;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {

    private CANRangeTimeOfFlight leftCANRange;
    private CANRangeTimeOfFlight rightCANRange;
    private CANRangeTimeOfFlight frontCenterCANRange;
    private CANRangeTimeOfFlight backCenterCANRange;

    private MotorController leftMotor;
    private MotorController rightMotor;

    private MotorController centerAlgaeMotor;

    private static final double CORAL_THRESHOLD = 200; // TODO: Set this to a real value

    private double leftPower = 0.25;
    private double rightPower = 0.25;

    private double algaePower = 0.35;

    public Intake() {
        leftCANRange =
                (CANRangeTimeOfFlight)
                        RobotProvider.instance.getTimeOfFlight("INTAKE.CANRange.left");
        rightCANRange =
                (CANRangeTimeOfFlight)
                        RobotProvider.instance.getTimeOfFlight("INTAKE.CANRange.right");
        frontCenterCANRange =
                (CANRangeTimeOfFlight)
                        RobotProvider.instance.getTimeOfFlight("INTAKE.CANRange.front_center");
        backCenterCANRange =
                (CANRangeTimeOfFlight)
                        RobotProvider.instance.getTimeOfFlight("INTAKE.CANRange.back_center");

        leftMotor = RobotProvider.instance.getMotor("INTAKE.leftIntakeMotor");
        rightMotor = RobotProvider.instance.getMotor("INTAKE.rightIntakeMotor");

        centerAlgaeMotor = RobotProvider.instance.getMotor("INTAKE.centerAlgaeMotor");
    }

    public record IntakeStatus(
            double leftDistance,
            double rightDistance,
            double frontCenterDistance,
            double backCenterDistance)
            implements Status {

        public boolean hasCoralInLeft() {
            return leftDistance < CORAL_THRESHOLD;
        }

        public boolean hasCoralInRight() {
            return rightDistance < CORAL_THRESHOLD;
        }

        public boolean hasCoralInFrontCenter() {
            return frontCenterDistance < CORAL_THRESHOLD;
        }

        public boolean hasCoralInBackCenter() {
            return backCenterDistance < CORAL_THRESHOLD;
        }
    }

    public void setLeft(double power) {
        leftMotor.set(power);
    }

    public void setRight(double power) {
        rightMotor.set(power);
    }

    public void setAlgae(double power) {
        centerAlgaeMotor.set(power);
    }

    public void setLeftPower(double power) {
        this.leftPower = power;
    }

    public void setRightPower(double power) {
        this.rightPower = power;
    }

    public void setAlgaePower(double power) {
        this.algaePower = power;
    }

    public void turnLeftPositive() {
        leftMotor.set(leftPower);
    }

    public void turnLeftNegative() {
        leftMotor.set(-leftPower);
    }

    public void turnRightPositive() {
        rightMotor.set(rightPower);
    }

    public void turnRightNegative() {
        rightMotor.set(-rightPower);
    }

    public void turnAlgaePositive() {
        centerAlgaeMotor.set(algaePower);
    }

    public void turnAlgaeNegative() {
        centerAlgaeMotor.set(-algaePower);
    }

    public void stop() {
        leftMotor.set(0);
        rightMotor.set(0);
        centerAlgaeMotor.set(0);
    }

    public void stopLeft() {
        leftMotor.set(0);
    }

    public void stopRight() {
        rightMotor.set(0);
    }

    public void stopAlgae() {
        centerAlgaeMotor.set(0);
    }

    @Override
    protected IntakeStatus updateStatus() {
        double leftDistance = leftCANRange.getDistance().orElse(Double.MAX_VALUE);
        double rightDistance = rightCANRange.getDistance().orElse(Double.MAX_VALUE);
        double frontCenterDistance = frontCenterCANRange.getDistance().orElse(Double.MAX_VALUE);
        double backCenterDistance = backCenterCANRange.getDistance().orElse(Double.MAX_VALUE);

        return new IntakeStatus(
                leftDistance, rightDistance, frontCenterDistance, backCenterDistance);
    }
}
