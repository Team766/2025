package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;
import static com.team766.math.Maths.normalizeAngleDegrees;

import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.SoftwareLimitSwitchConfigs;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.math.Maths;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Hood extends MechanismWithStatus<Hood.HoodStatus> {
    private static final double AT_ROTATIONAL_ANGLE_THRESHOLD = 3.0; // TODO: Find actual value

    public record HoodStatus(double angle) implements Status {
        public boolean isAtAngle(double targetAngle) {
            return Math.abs(normalizeAngleDegrees(targetAngle - angle))
                    < AT_ROTATIONAL_ANGLE_THRESHOLD;
        }
    }

    // Gearing stages:
    //   motor
    //   12 : 62
    //   minorEncoder
    //   14 : 64
    //   majorEncoder
    //   18 : 248
    //   hood
    private static final int MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_DENOMINATOR = 14;
    private static final int MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_NUMERATOR = 64;
    private static final double MOTOR_TO_MECHANISM_GEAR_RATIO =
            (248.0 / 18.0) * (64.0 / 14.0) * (62.0 / 12.0);
    private static final double MAJOR_ENCODER_TO_MECHANISM_GEAR_RATIO = (248.0 / 18.0);
    private static final double ZERO_POSITION_OFFSET = 0.0;

    private static final double MAX_ANGLE = 65;
    private static final double MIN_ANGLE = 30;

    private final TalonFX motor;
    private final CANcoder majorEncoder;
    private final CANcoder minorEncoder;

    public Hood() {
        motor = new TalonFX(HardwareConfig.Motor.HOOD.canId(), HardwareConfig.Motor.HOOD.canBus());
        majorEncoder =
                new CANcoder(
                        HardwareConfig.CANcoder.HOOD_MAJOR.canId(),
                        HardwareConfig.CANcoder.HOOD_MAJOR.canBus());
        minorEncoder =
                new CANcoder(
                        HardwareConfig.CANcoder.HOOD_MINOR.canId(),
                        HardwareConfig.CANcoder.HOOD_MINOR.canBus());

        var feedbackConfig = new FeedbackConfigs();
        feedbackConfig.SensorToMechanismRatio = MOTOR_TO_MECHANISM_GEAR_RATIO;
        statusCodeToException(ExceptionTarget.THROW, motor.getConfigurator().apply(feedbackConfig));
        var limitConfig = new SoftwareLimitSwitchConfigs();
        limitConfig.ForwardSoftLimitEnable = true;
        limitConfig.ForwardSoftLimitThreshold = MAX_ANGLE;
        limitConfig.ReverseSoftLimitEnable = true;
        limitConfig.ReverseSoftLimitThreshold = MIN_ANGLE;
        statusCodeToException(ExceptionTarget.THROW, motor.getConfigurator().apply(limitConfig));

        final double encoderPosition =
                Maths.chineseRemainder(
                                majorEncoder.getAbsolutePosition().getValueAsDouble()
                                        * MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_NUMERATOR,
                                MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_NUMERATOR,
                                minorEncoder.getAbsolutePosition().getValueAsDouble()
                                        * MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_DENOMINATOR,
                                MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_DENOMINATOR)
                        / MINOR_ENCODER_TO_MAJOR_ENCODER_GEAR_NUMERATOR;
        final double position =
                (1.0 / MAJOR_ENCODER_TO_MECHANISM_GEAR_RATIO) * encoderPosition
                        + ZERO_POSITION_OFFSET;
        statusCodeToException(ExceptionTarget.THROW, motor.setPosition(position));
    }

    public void setTargetAngle(double angle) {
        motor.setControl(new PositionVoltage(angle));
    }

    public void move(double power) {
        motor.setVoltage(12 * power);
    }

    public void stop() {
        motor.stopMotor();
    }

    @Override
    protected HoodStatus updateStatus() {
        return new HoodStatus(motor.getPosition(false).getValueAsDouble());
    }
}
