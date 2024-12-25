package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends Mechanism<Climber.ClimberStatus> {
    public static class Position {
        // A very rough measurement, and was being very safe.
        // TODO: Needs to be measured more accurately.
        public static final double TOP = 43.18;
        public static final double BOTTOM = 0;
        public static final double BELOW_ARM = 15; // TODO: Find actual value
    }

    public record ClimberStatus(double heightLeft, double heightRight) implements Status {
        public boolean isLeftNear(double position) {
            return Math.abs(heightLeft() - position) < POSITION_LOCATION_THRESHOLD;
        }

        public boolean isRightNear(double position) {
            return Math.abs(heightRight() - position) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public Request<Climber> requestStop() {
        return setRequest(requestAllOf(leftMotor.requestStop(), rightMotor.requestStop()));
    }

    public Request<Climber> requestMotorPowers(
            double powerLeft, double powerRight, boolean overrideSoftLimits) {
        boolean enableSoftLimits = !overrideSoftLimits;
        if (enableSoftLimits != softLimitsEnabled) {
            enableSoftLimits(enableSoftLimits);
        }
        return setRequest(
                requestAllOf(
                        leftMotor.requestPercentOutput(
                                com.team766.math.Math.clamp(powerLeft, -1, 1)),
                        rightMotor.requestPercentOutput(
                                com.team766.math.Math.clamp(powerRight, -1, 1))));
    }

    public Request<Climber> requestPosition(double targetHeight) {
        if (!softLimitsEnabled) {
            enableSoftLimits(true);
        }

        return setRequest(
                () -> {
                    // Control left motor
                    boolean isLeftNear = false;
                    if (getStatus().isLeftNear(targetHeight)) {
                        leftMotor.stopMotor();
                        isLeftNear = true;
                    } else if (getStatus().heightLeft() > targetHeight) {
                        // Move down
                        leftMotor.set(0.25);
                    } else {
                        // Move up
                        leftMotor.set(-0.25);
                    }

                    // Control right motor
                    boolean isRightNear = false;
                    if (getStatus().isRightNear(targetHeight)) {
                        rightMotor.stopMotor();
                        isRightNear = true;
                    } else if (getStatus().heightRight() > targetHeight) {
                        // Move down
                        rightMotor.set(0.25);
                    } else {
                        // Move up
                        rightMotor.set(-0.25);
                    }

                    return isLeftNear && isRightNear;
                });
    }

    private MotorController leftMotor;
    private MotorController rightMotor;

    private static final double GEAR_RATIO_AND_CIRCUMFERENCE =
            (14. / 50.) * (30. / 42.) * (1.25 * Math.PI);
    private static final double SUPPLY_CURRENT_LIMIT = 30; // max efficiency from spec sheet
    private static final double STATOR_CURRENT_LIMIT = 80; // TUNE THIS!
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private static final double INITITAL_POSITION = -63.0; // TODO: set
    private static final double NUDGE_INCREMENT = 0.1;

    private boolean softLimitsEnabled;

    public Climber() {
        leftMotor = RobotProvider.instance.getMotor(CLIMBER_LEFT_MOTOR);
        rightMotor = RobotProvider.instance.getMotor(CLIMBER_RIGHT_MOTOR);

        leftMotor.setNeutralMode(NeutralMode.Brake);
        rightMotor.setNeutralMode(NeutralMode.Brake);
        leftMotor.setCurrentLimit(SUPPLY_CURRENT_LIMIT);
        rightMotor.setCurrentLimit(SUPPLY_CURRENT_LIMIT);
        leftMotor.setSensorPosition(INITITAL_POSITION);
        rightMotor.setSensorPosition(INITITAL_POSITION);
        MotorUtil.setTalonFXStatorCurrentLimit(leftMotor, STATOR_CURRENT_LIMIT);
        MotorUtil.setTalonFXStatorCurrentLimit(rightMotor, STATOR_CURRENT_LIMIT);
        MotorUtil.setSoftLimits(leftMotor, 0.0 /* forward limit */, -115.0 /* reverse limit */);
        MotorUtil.setSoftLimits(rightMotor, 0.0 /* forward limit */, -115.0 /* reverse limit */);

        enableSoftLimits(true);
    }

    private void enableSoftLimits(boolean enabled) {
        MotorUtil.enableSoftLimits(leftMotor, enabled);
        MotorUtil.enableSoftLimits(rightMotor, enabled);
        softLimitsEnabled = enabled;
    }

    public void resetLeftPosition() {
        leftMotor.setSensorPosition(0);
    }

    public void resetRightPosition() {
        rightMotor.setSensorPosition(0);
    }

    private static double heightToRotations(double height) {
        return height * GEAR_RATIO_AND_CIRCUMFERENCE;
    }

    private static double rotationsToHeight(double rotations) {
        return rotations / GEAR_RATIO_AND_CIRCUMFERENCE;
    }

    @Override
    protected ClimberStatus reportStatus() {
        // SmartDashboard.putNumber("[CLIMBER] Left Rotations", leftMotor.getSensorPosition());
        // SmartDashboard.putNumber("[CLIMBER] Right Rotations", rightMotor.getSensorPosition());
        // SmartDashboard.putNumber("[CLIMBER] Left Height", getHeightLeft());
        // SmartDashboard.putNumber("[CLIMBER] Right Height", getHeightRight());
        // SmartDashboard.putNumber("[CLIMBER] Left Power", leftPower);
        // SmartDashboard.putNumber("[CLIMBER] Right Power", rightPower);
        // SmartDashboard.putNumber(
        //         "[CLIMBER] Left Motor Supply Current", MotorUtil.getCurrentUsage(leftMotor));
        // SmartDashboard.putNumber(
        //         "[CLIMBER] Right Motor Supply Current", MotorUtil.getCurrentUsage(rightMotor));
        // SmartDashboard.putNumber(
        //         "[CLIMBER] Left Motor Stator Current",
        // MotorUtil.getStatorCurrentUsage(leftMotor));
        // SmartDashboard.putNumber(
        //         "[CLIMBER] Right Motor Stator Current",
        //         MotorUtil.getStatorCurrentUsage(rightMotor));
        return new ClimberStatus(
                rotationsToHeight(leftMotor.getSensorPosition()),
                rotationsToHeight(rightMotor.getSensorPosition()));
    }
}
