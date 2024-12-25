package com.team766.robot.burro_arm.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;
import com.team766.library.ValueProvider;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Arm extends Mechanism<Arm.ArmStatus> {
    public record ArmStatus(double angle) implements Status {}

    public Request<Arm> requestPercentOutput(double percentOutput) {
        return setRequest(motor.requestPercentOutput(percentOutput));
    }

    public Request<Arm> requestAngle(double targetAngle) {
        return setRequest(
                motor.requestPosition(
                        targetAngle / MOTOR_ROTATIONS_TO_ARM_ANGLE,
                        ANGLE_TOLERANCE / MOTOR_ROTATIONS_TO_ARM_ANGLE,
                        STOPPED_SPEED_THRESHOLD / MOTOR_ROTATIONS_TO_ARM_ANGLE * 60 /* RPMs */));
    }

    public Request<Arm> requestHoldPosition() {
        final double currentAngle = getStatus().angle();
        return requestAngle(currentAngle);
    }

    public Request<Arm> requestNudgeUp() {
        final double currentAngle = getStatus().angle();
        return requestAngle(currentAngle + NUDGE_UP_INCREMENT);
    }

    public Request<Arm> requestNudgeDown() {
        final double currentAngle = getStatus().angle();
        return requestAngle(currentAngle - NUDGE_DOWN_INCREMENT);
    }

    private static final double NUDGE_UP_INCREMENT = 5.0; // degrees
    private static final double NUDGE_DOWN_INCREMENT = 5.0; // degrees

    private static final double ANGLE_TOLERANCE = 3; // degrees
    private static final double STOPPED_SPEED_THRESHOLD = 3; // degrees/sec

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
        motor.setSmartCurrentLimit(5, 80, 200);
        absoluteEncoder = RobotProvider.instance.getEncoder("arm.AbsoluteEncoder");
        absoluteEncoderOffset = ConfigFileReader.instance.getDouble("arm.AbsoluteEncoderOffset");

        requestPercentOutput(0);
    }

    @Override
    protected Request<Arm> applyIdleRequest() {
        return requestHoldPosition();
    }

    @Override
    protected ArmStatus reportStatus() {
        if (!initialized && absoluteEncoder.isConnected()) {
            final double absoluteEncoderPosition =
                    Math.IEEEremainder(
                            absoluteEncoder.getPosition() + absoluteEncoderOffset.get(), 1.0);
            SmartDashboard.putNumber(
                    "[ARM] AbsoluteEncoder Init Position", absoluteEncoderPosition);
            motor.setSensorPosition(
                    absoluteEncoderPosition
                            * ABSOLUTE_ENCODER_TO_ARM_ANGLE
                            / MOTOR_ROTATIONS_TO_ARM_ANGLE);
            initialized = true;
        }

        return new ArmStatus(motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ARM_ANGLE);
    }
}
