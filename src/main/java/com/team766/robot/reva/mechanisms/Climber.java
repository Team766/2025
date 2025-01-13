package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Climber extends MechanismWithStatus<Climber.ClimberStatus> {

    public record ClimberStatus(double heightLeft, double heightRight) implements Status {
        public boolean isLeftNear(ClimberPosition position) {
            return Math.abs(heightLeft() - position.getHeight()) < POSITION_LOCATION_THRESHOLD;
        }

        public boolean isRightNear(ClimberPosition position) {
            return Math.abs(heightRight() - position.getHeight()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum ClimberPosition {
        // A very rough measurement, and was being very safe.
        // TODO: Needs to be measured more accurately.
        TOP(43.18),
        BOTTOM(0);

        private final double height;

        private ClimberPosition(double height) {
            this.height = height;
        }

        public double getHeight() {
            return height;
        }

        public double getRotations() {
            return heightToRotations(height);
        }
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

    public void enableSoftLimits(boolean enabled) {
        checkContextReservation();
        MotorUtil.enableSoftLimits(leftMotor, enabled);
        MotorUtil.enableSoftLimits(rightMotor, enabled);
    }

    public void resetLeftPosition() {
        checkContextReservation();
        leftMotor.setSensorPosition(0);
    }

    public void resetRightPosition() {
        checkContextReservation();
        rightMotor.setSensorPosition(0);
    }

    public void setPower(double power) {
        checkContextReservation();
        setLeftPower(power);
        setRightPower(power);
    }

    public void setLeftPower(double power) {
        checkContextReservation();
        power = com.team766.math.Math.clamp(power, -1, 1);
        leftMotor.set(power);
    }

    public void setRightPower(double power) {
        checkContextReservation();
        power = com.team766.math.Math.clamp(power, -1, 1);
        rightMotor.set(power);
    }

    public void stop() {
        checkContextReservation();
        stopLeft();
        stopRight();
    }

    public void stopLeft() {
        checkContextReservation();
        leftMotor.stopMotor();
    }

    public void stopRight() {
        checkContextReservation();
        rightMotor.stopMotor();
    }

    @Override
    protected void onMechanismIdle() {
        stopLeft();
        stopRight();
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
