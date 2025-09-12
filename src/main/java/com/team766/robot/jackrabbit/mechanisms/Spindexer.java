package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Spindexer extends MechanismWithStatus<Spindexer.SpindexerStatus> {
    public record SpindexerStatus() implements Status {}

    // Gearing stages:
    //   motor
    //   1 : 9 planetary stage
    //   1 : 5 planetary stage
    //   18 : 36 belt
    //   spindexer
    private static final double MOTOR_TO_MECHANISM_GEAR_RATIO =
            (36.0 / 18.0) * (5.0 / 1.0) * (9.0 / 1.0);

    private final TalonFX motor;

    public Spindexer() {
        motor =
                new TalonFX(
                        HardwareConfig.Motor.SPINDEXER.canId(),
                        HardwareConfig.Motor.SPINDEXER.canBus());

        var feedbackConfig = new FeedbackConfigs();
        feedbackConfig.SensorToMechanismRatio = MOTOR_TO_MECHANISM_GEAR_RATIO;
        statusCodeToException(ExceptionTarget.THROW, motor.getConfigurator().apply(feedbackConfig));
    }

    public void stop() {
        motor.stopMotor();
    }

    public void move() {
        motor.setControl(new VelocityVoltage(1));
    }

    public void reverse() {
        motor.set(-0.5);
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected SpindexerStatus updateStatus() {
        return new SpindexerStatus();
    }
}
