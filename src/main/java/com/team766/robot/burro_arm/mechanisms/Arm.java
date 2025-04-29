package com.team766.robot.burro_arm.mechanisms;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;
import com.team766.library.ValueProvider;

public class Arm extends MechanismWithStatus<Arm.ArmStatus> {
    public record ArmStatus(double angle) implements Status {}

    private static final double ABSOLUTE_ENCODER_TO_ARM_ANGLE =
            (360. /*degrees per rotation*/) * (12. / 54. /*chain reduction*/);
    private static final double MOTOR_ROTATIONS_TO_ARM_ANGLE =
            ABSOLUTE_ENCODER_TO_ARM_ANGLE * (1. / (5. * 5. * 5.) /*planetary gearbox*/);

    private final CANSparkMaxMotorController motor;
    private final EncoderReader absoluteEncoder;

    private final ValueProvider<Double> absoluteEncoderOffset;

    private boolean initialized = false;

    public Arm() {
        motor = (CANSparkMaxMotorController) RobotProvider.instance.getMotor("arm.Motor");
        motor.configure(
                new SparkMaxConfig().smartCurrentLimit(5, 80, 200),
                SparkMax.ResetMode.kNoResetSafeParameters,
                SparkMax.PersistMode.kPersistParameters);
        absoluteEncoder = RobotProvider.instance.getEncoder("arm.AbsoluteEncoder");
        absoluteEncoderOffset = ConfigFileReader.instance.getDouble("arm.AbsoluteEncoderOffset");
    }

    public void setPower(final double power) {
        motor.set(power);
    }

    public void setAngle(final double angle) {
        motor.set(ControlMode.Position, angle / MOTOR_ROTATIONS_TO_ARM_ANGLE);
    }

    @Override
    protected ArmStatus updateStatus() {
        return new ArmStatus(motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ARM_ANGLE);
    }

    @Override
    public void run() {
        if (!initialized && absoluteEncoder.isConnected()) {
            final double absoluteEncoderPosition =
                    Math.IEEEremainder(
                            absoluteEncoder.getPosition() + absoluteEncoderOffset.get(), 1.0);
            motor.setSensorPosition(
                    absoluteEncoderPosition
                            * ABSOLUTE_ENCODER_TO_ARM_ANGLE
                            / MOTOR_ROTATIONS_TO_ARM_ANGLE);
            initialized = true;
        }
    }
}
