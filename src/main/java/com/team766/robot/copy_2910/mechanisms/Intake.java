package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.*;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANRangeTimeOfFlight;
import com.team766.logging.Severity;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {

    private CANRangeTimeOfFlight leftCANRange;
    private CANRangeTimeOfFlight rightCANRange;
    private CANRangeTimeOfFlight frontCenterCANRange;
    private CANRangeTimeOfFlight backCenterCANRange;

    private MotorController leftMotor;
    private MotorController rightMotor;

    private static final double CORAL_THRESHOLD = 200; // TODO: Set this to a real value

    public Intake() {
        leftCANRange = new CANRangeTimeOfFlight(-1); // TODO: Fix these with real IDs
        rightCANRange = new CANRangeTimeOfFlight(-1); // TODO: Fix these with real IDs
        frontCenterCANRange = new CANRangeTimeOfFlight(-1); // TODO: Fix these with real IDs
        backCenterCANRange = new CANRangeTimeOfFlight(-1); // TODO: Fix these with real IDs

        leftMotor = RobotProvider.instance.getMotor("leftIntakeMotor");
        rightMotor = RobotProvider.instance.getMotor("rightIntakeMotor");
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

    @Override
    protected IntakeStatus updateStatus() {
        try {
            double leftDistance = leftCANRange.getDistance().get();
            double rightDistance = rightCANRange.getDistance().get();
            double frontCenterDistance = frontCenterCANRange.getDistance().get();
            double backCenterDistance = backCenterCANRange.getDistance().get();

            return new IntakeStatus(
                    leftDistance, rightDistance, frontCenterDistance, backCenterDistance);
        } catch (Exception e) {
            log(
                    "Error reading CAN Range Time of Flight sensors: " + e.getMessage(),
                    Severity.ERROR);
        }

        return new IntakeStatus(
                Double.MAX_VALUE, Double.MAX_VALUE,
                Double.MAX_VALUE, Double.MAX_VALUE);
    }
}
