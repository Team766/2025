package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.hal.wpilib.CTREPhoenix6Utils.statusCodeToException;
import static com.team766.math.Maths.normalizeAngleDegrees;

import com.ctre.phoenix6.configs.ExternalFeedbackConfigs;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.signals.ExternalFeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.ForwardLimitValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.ReverseLimitValue;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.wpilib.CTREPhoenix6Utils.ExceptionTarget;
import com.team766.robot.jackrabbit.HardwareConfig;

public class Turret extends MechanismWithStatus<Turret.TurretStatus> {
    private static final double AT_ROTATIONAL_ANGLE_THRESHOLD = 3.0; // TODO: Find actual value

    public record GyroHeading(double heading) {}

    public record TurretStatus(
            boolean initialized,
            double angle,
            double gyroHeading,
            GyroHeading targetGyroHeading,
            boolean leftMagnetSensor,
            boolean rightMagnetSensor)
            implements Status {
        public boolean isAtGyroHeading(GyroHeading targetGyroHeading) {
            return Math.abs(normalizeAngleDegrees(targetGyroHeading.heading - gyroHeading))
                    < AT_ROTATIONAL_ANGLE_THRESHOLD;
        }

        public boolean isAtTarget() {
            return isAtGyroHeading(targetGyroHeading());
        }
    }

    private interface Mode {
        void run();

        GyroHeading getTargetGyroHeading();
    }

    // Gearing stages:
    //   motor
    //   1 : 9 planetary stage
    //   18 : 128 belt
    //   turret
    private static final double MOTOR_TO_MECHANISM_GEAR_RATIO = (128.0 / 18.0) * (9.0 / 1.0);

    private static final double LEFT_SENSOR_LEFT_ANGLE = 22;
    private static final double LEFT_SENSOR_RIGHT_ANGLE = 20;
    private static final double RIGHT_SENSOR_LEFT_ANGLE = -20;
    private static final double RIGHT_SENSOR_RIGHT_ANGLE = -22;

    private static final double MIN_ANGLE = -200;
    private static final double MAX_ANGLE = 200;

    private final TalonFXS motor;
    private final Pigeon2 gyro;

    private boolean initialized = false;
    private Mode mode;

    public Turret() {
        motor =
                new TalonFXS(
                        HardwareConfig.Motor.TURRET.canId(), HardwareConfig.Motor.TURRET.canBus());
        gyro =
                new Pigeon2(
                        HardwareConfig.Pigeon.TURRET.canId(),
                        HardwareConfig.Pigeon.TURRET.canBus());

        statusCodeToException(ExceptionTarget.THROW, motor.setNeutralMode(NeutralModeValue.Brake));

        ExternalFeedbackConfigs feedbackConfig = new ExternalFeedbackConfigs();
        feedbackConfig.FeedbackRemoteSensorID = gyro.getDeviceID();
        feedbackConfig.ExternalFeedbackSensorSource =
                ExternalFeedbackSensorSourceValue.RemotePigeon2_Yaw;
        feedbackConfig.RotorToSensorRatio = MOTOR_TO_MECHANISM_GEAR_RATIO;
        statusCodeToException(ExceptionTarget.THROW, motor.getConfigurator().apply(feedbackConfig));

        var gyroConfig = new Pigeon2Configuration();
        // TODO: gyroConfig.MountPose
        statusCodeToException(ExceptionTarget.THROW, gyro.getConfigurator().apply(gyroConfig));

        stop();
    }

    public GyroHeading setTargetAngle(double targetAngle) {
        if (!initialized) {
            throw new IllegalStateException("Turret ia not initialized yet");
        }
        final double targetHeading = targetAngle - getStatus().angle() + getStatus().gyroHeading();
        var targetGyroHeading = new GyroHeading(targetHeading);
        mode = new Mode() {
            public void run() {
                motor.setControl(new PositionVoltage(targetHeading));
            }

            public GyroHeading getTargetGyroHeading() {
                return targetGyroHeading;
            }
        };
        return targetGyroHeading;
    }

    public void move(double power) {
        mode = new Mode() {
            @Override
            public void run() {
                motor.setVoltage(12 * power);
            }

            @Override
            public GyroHeading getTargetGyroHeading() {
                return new GyroHeading(getStatus().gyroHeading());
            }
        };
    }

    public void stop() {
        mode = new Mode() {
            @Override
            public void run() {
                motor.stopMotor();
            }

            @Override
            public GyroHeading getTargetGyroHeading() {
                return new GyroHeading(getStatus().gyroHeading());
            }
        };
    }

    public void moveCCWForInitialization() {
        initialized = false;
        mode = new Mode() {
            @Override
            public void run() {
                motor.set(0.15);
            }

            @Override
            public GyroHeading getTargetGyroHeading() {
                return new GyroHeading(getStatus().gyroHeading());
            }
        };
    }

    @Override
    protected TurretStatus updateStatus() {
        return new TurretStatus(
                initialized,
                motor.getRotorPosition(false).getValueAsDouble(),
                motor.getPosition(false).getValueAsDouble(),
                mode.getTargetGyroHeading(),
                motor.getForwardLimit(false).getValue() == ForwardLimitValue.ClosedToGround,
                motor.getReverseLimit(false).getValue() == ReverseLimitValue.ClosedToGround);
    }

    @Override
    protected void run() {
        if (!initialized) {
            if (getStatus().leftMagnetSensor()) {
                if (motor.getVelocity(false).getValueAsDouble() >= 0) {
                    // Moving CCW - right edge of left sensor
                    motor.setPosition(LEFT_SENSOR_RIGHT_ANGLE);
                } else {
                    // Moving CW - left edge of left sensor
                    if (getStatus().angle() > -60) {
                        motor.setPosition(LEFT_SENSOR_LEFT_ANGLE);
                    } else {
                        // The mechanism has moved for a while, so it's almost certainly wrapped
                        // around.
                        motor.setPosition(-360 + LEFT_SENSOR_LEFT_ANGLE);
                    }
                }
                initialized = true;
            } else if (getStatus().rightMagnetSensor()) {
                if (motor.getVelocity(false).getValueAsDouble() <= 0) {
                    // Moving CW - left edge of right sensor
                    motor.setPosition(RIGHT_SENSOR_LEFT_ANGLE);
                } else {
                    // Moving CCW - right edge of right sensor
                    if (getStatus().angle() < 60) {
                        motor.setPosition(RIGHT_SENSOR_RIGHT_ANGLE);
                    } else {
                        // The mechanism has moved for a while, so it's almost certainly wrapped
                        // around.
                        motor.setPosition(360 + RIGHT_SENSOR_RIGHT_ANGLE);
                    }
                }
                initialized = true;
            }
        }

        // Unwind saturated turret
        final double targetAngle =
            mode.getTargetGyroHeading().heading() - getStatus().gyroHeading() + getStatus().angle();
        if (targetAngle > MAX_ANGLE || targetAngle < MIN_ANGLE) {
            final double unwoundTargetAngle = normalizeAngleDegrees(targetAngle);
            setTargetAngle(unwoundTargetAngle);
        }
        
        mode.run();
    }
}
