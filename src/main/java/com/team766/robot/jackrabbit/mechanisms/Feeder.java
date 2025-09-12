package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Feeder extends MechanismWithStatus<Feeder.FeederStatus> {
    public record FeederStatus() implements Status {}

    // Gearing stages:
    //   motor
    //   12 : 42
    //   feeder wheels
    private static final double MOTOR_TO_MECHANISM_GEAR_RATIO = (42.0 / 12.0);

    private static final double FEED_SPEED = 50;
    private static final double INDEX_SPEED = 30;

    private final TalonFX leftMotor;
    private final TalonFX rightMotor;

    public Feeder() {
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
        statusCodeToException(
                ExceptionTarget.THROW, rightMotor.getConfigurator().apply(feedbackConfig));
    }

    public void stop() {
        leftMotor.stopMotor();
        rightMotor.stopMotor();
    }

    public void feed() {
        leftMotor.setControl(new VelocityVoltage(FEED_SPEED));
        rightMotor.setControl(new VelocityVoltage(-FEED_SPEED));
    }

    public void index() {
        leftMotor.setControl(new VelocityVoltage(INDEX_SPEED));
        rightMotor.setControl(new VelocityVoltage(INDEX_SPEED));
    }

    public void reverse() {
        leftMotor.set(-0.8);
        rightMotor.set(-0.4);
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected FeederStatus updateStatus() {
        return new FeederStatus();
    }
}
