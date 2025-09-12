package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.controls.StrictFollower;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {
    private static final double AT_SPEED_THRESHOLD = 3.0; // TODO: Find actual value

    public record ShooterStatus(double speed, double targetSpeed) implements Status {
        public boolean isAtSpeed(double targetSpeed) {
            return Math.abs(targetSpeed - speed) < AT_SPEED_THRESHOLD;
        }

        public boolean isAtTarget() {
            return isAtSpeed(targetSpeed());
        }
    }

    // Gearing stages:
    //   motor
    //   14 : 20
    //   spindexer
    private static final double MOTOR_TO_MECHANISM_GEAR_RATIO = (20.0 / 14.0);

    private final TalonFX leftMotor;
    private final TalonFX rightMotor;

    private double targetSpeed = Double.POSITIVE_INFINITY;

    public Shooter() {
        leftMotor =
                new TalonFX(
                        HardwareConfig.Motor.SHOOTER_LEFT.canId(),
                        HardwareConfig.Motor.SHOOTER_LEFT.canBus());
        rightMotor =
                new TalonFX(
                        HardwareConfig.Motor.SHOOTER_RIGHT.canId(),
                        HardwareConfig.Motor.SHOOTER_RIGHT.canBus());

        var feedbackConfig = new FeedbackConfigs();
        feedbackConfig.SensorToMechanismRatio = MOTOR_TO_MECHANISM_GEAR_RATIO;
        statusCodeToException(
                ExceptionTarget.THROW, leftMotor.getConfigurator().apply(feedbackConfig));

        StrictFollower follower = new StrictFollower(leftMotor.getDeviceID());
        statusCodeToException(ExceptionTarget.THROW, rightMotor.setControl(follower));
    }

    public void stop() {
        leftMotor.stopMotor();
        targetSpeed = Double.POSITIVE_INFINITY;
    }

    public void shoot(double speed) {
        leftMotor.setControl(new VelocityVoltage(speed));
        targetSpeed = speed;
    }

    public void reverse() {
        leftMotor.set(-0.3);
        targetSpeed = Double.POSITIVE_INFINITY;
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected ShooterStatus updateStatus() {
        return new ShooterStatus(leftMotor.getVelocity(false).getValueAsDouble(), targetSpeed);
    }
}
