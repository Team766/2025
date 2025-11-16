package com.team766.robot.mayhem_shooter.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {
    private MotorController shooterMotor;
    private MotorController feederMotor;
    private MotorController intakeMotor;
    private double targetShooterSpeed;
    private static final double SHOOTER_SPEED_TOLERANCE = 5;
    private static final double DEFAULT_SHOOTER_SPEED = 50;
    private static final double DEFAULT_FEEDER_SPEED = 50;

    public static record ShooterStatus(double shooterRPS, double targetSpeed) implements Status {
        public boolean isAtTargetSpeed() {
            return Math.abs(shooterRPS - targetSpeed) < SHOOTER_SPEED_TOLERANCE;
        }
    }

    public Shooter() {
        shooterMotor = RobotProvider.instance.getMotor("mayhemShooter.shooterMotor");
        feederMotor = RobotProvider.instance.getMotor("mayhemShooter.feederMotor");
        intakeMotor = RobotProvider.instance.getMotor("intakeMotor");

        shooterMotor.setCurrentLimit(50);
        feederMotor.setCurrentLimit(50);
    }

    public void setShooterPower(final double percent) {
        targetShooterSpeed = percent;
        shooterMotor.set(ControlMode.PercentOutput, percent);
    }

    public double getShooterPower() {
        return shooterMotor.get();
    }

    public void enableShooter() {
        setShooterPower(0.40);
    }

    public void setIntakeMotor(double power) {
        intakeMotor.set(power);
    }

    public void stopShooterMotor() {
        shooterMotor.stopMotor();
    }

    public void setFeederPower(final double feederPower) {
        feederMotor.set(feederPower);
    }

    public void enableFeeder() {
        setFeederPower(DEFAULT_FEEDER_SPEED);
    }

    @Override
    protected void onMechanismIdle() {
        // Stop mechanism when nothing is using it.
        stopShooterMotor();
        setFeederPower(0);
        //setIntakeMotor(0);
    }

    @Override
    protected ShooterStatus updateStatus() {
        return new ShooterStatus(shooterMotor.getSensorVelocity(), targetShooterSpeed);
    }
}
