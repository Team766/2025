package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_ENCODER;
import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_LEFT;
import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_RIGHT;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.framework3.requests.RequestForPositionControl;
import com.team766.framework3.requests.RequestForStop;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.REVThroughBoreDutyCycleEncoder;
import com.team766.library.ValueProvider;

public class Shoulder extends Mechanism<Shoulder, Shoulder.ShoulderStatus> {
    public static class Position {
        // TODO: Find actual values.
        public static final double BOTTOM = 0;
        public static final double INTAKE_FLOOR = 0;
        public static final double SHOOT_LOW = 15;
        public static final double SHOOTER_ASSIST = 18.39;
        public static final double SHOOT_MEDIUM = 30;
        public static final double SHOOT_HIGH = 80;
        public static final double AMP = 90;
        public static final double TOP =
                105; // angle needed to be upped so it works with the climber
    }

    public record ShoulderStatus(double angle) implements Status {
        public boolean isNearTo(double targetAngle) {
            return Math.abs(angle() - targetAngle) < NEAR_THRESHOLD;
        }
    }

    public Request<Shoulder> requestForStop() {
        return new RequestForStop<>(leftMotor);
    }

    public Request<Shoulder> requestForHoldPosition() {
        final double currentAngle = getStatus().angle();
        return requestForPosition(currentAngle);
    }

    public Request<Shoulder> requestForNudgeUp() {
        final double currentAngle = getStatus().angle();
        return requestForPosition(currentAngle + NUDGE_AMOUNT);
    }

    public Request<Shoulder> requestForNudgeDown() {
        final double currentAngle = getStatus().angle();
        return requestForPosition(currentAngle - NUDGE_AMOUNT);
    }

    public Request<Shoulder> requestForPosition(double targetAngle) {
        targetAngle = com.team766.math.Math.clamp(targetAngle, Position.BOTTOM, Position.TOP);
        return new RequestForPositionControl<>(
                leftMotor,
                degreesToRotations(targetAngle),
                degreesToRotations(NEAR_THRESHOLD),
                degreesToRotations(STOPPED_VELOCITY_THRESHOLD),
                () -> ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().angle())));
    }

    private static final double NUDGE_AMOUNT = 1; // degrees
    private static final double ENCODER_INITIALIZATION_LOOPS = 350;

    private static final double NEAR_THRESHOLD = 2.5; // degrees
    private static final double STOPPED_VELOCITY_THRESHOLD = 2.5; // degrees/sec

    private final REVThroughBoreDutyCycleEncoder absoluteEncoder;
    private int encoderInitializationCount = 0;
    private static final double SUPPLY_CURRENT_LIMIT = 30.0; // max efficiency from spec sheet
    private static final double STATOR_CURRENT_LIMIT = 80.0; // TUNE THIS!
    private static final double DEFAULT_POSITION = 77.0;

    private MotorController leftMotor;
    private MotorController rightMotor;

    private ValueProvider<Double> ffGain;

    public Shoulder() {
        // TODO: Initialize and use CANCoders to get offset for relative encoder on boot.
        leftMotor = RobotProvider.instance.getMotor(SHOULDER_LEFT);
        rightMotor = RobotProvider.instance.getMotor(SHOULDER_RIGHT);
        rightMotor.follow(leftMotor);

        leftMotor.setNeutralMode(NeutralMode.Brake);
        rightMotor.setNeutralMode(NeutralMode.Brake);
        leftMotor.setCurrentLimit(SUPPLY_CURRENT_LIMIT);
        rightMotor.setCurrentLimit(SUPPLY_CURRENT_LIMIT);
        MotorUtil.setTalonFXStatorCurrentLimit(leftMotor, STATOR_CURRENT_LIMIT);
        MotorUtil.setTalonFXStatorCurrentLimit(rightMotor, STATOR_CURRENT_LIMIT);

        ffGain = ConfigFileReader.getInstance().getDouble("shoulder.leftMotor.ffGain");

        absoluteEncoder =
                (REVThroughBoreDutyCycleEncoder)
                        RobotProvider.instance.getEncoder(SHOULDER_ENCODER);
        leftMotor.setSensorPosition(DEFAULT_POSITION);
    }

    public void reset() {
        checkContextReservation();
        leftMotor.setSensorPosition(0.0);
        setRequest(requestForStop());
    }

    @Override
    protected ShoulderStatus reportStatus() {
        // encoder takes some time to settle.
        // this threshold was determined very scientifically around 3:20am.
        final double absPos = absoluteEncoder.getAbsolutePosition();
        if (encoderInitializationCount < ENCODER_INITIALIZATION_LOOPS
                && absoluteEncoder.isConnected()) {
            double convertedPos = absoluteEncoderToMotorRotations(absPos - 0.071);
            // TODO: only set the sensor position after this has settled?
            // can try in the next round of testing.
            leftMotor.setSensorPosition(convertedPos);
            encoderInitializationCount++;
        }
        final double rotations = leftMotor.getSensorPosition();
        final double angle = rotationsToDegrees(rotations);
        // SmartDashboard.putNumber("[SHOULDER] Rotations", rotations);
        // SmartDashboard.putNumber("[SHOULDER] Encoder Frequency", absoluteEncoder.getFrequency());
        // SmartDashboard.putNumber("[SHOULDER] Absolute Encoder Position", absPos);
        // SmartDashboard.putNumber(
        //         "[SHOULDER] Left Motor Supply Current", MotorUtil.getCurrentUsage(leftMotor));
        // SmartDashboard.putNumber(
        //         "[SHOULDER] Right Motor Supply Current", MotorUtil.getCurrentUsage(rightMotor));
        // SmartDashboard.putNumber(
        //         "[SHOULDER] Left Motor Stator Current",
        // MotorUtil.getStatorCurrentUsage(leftMotor));
        // SmartDashboard.putNumber(
        //         "[SHOULDER] Right Motor Stator Current",
        //         MotorUtil.getStatorCurrentUsage(rightMotor));
        // SmartDashboard.putNumber("[SHOULDER VELOCITY]", Math.abs(leftMotor.getSensorVelocity()));

        return new ShoulderStatus(angle);
    }

    private double degreesToRotations(double angle) {
        // angle * sprocket ratio * net gear ratio * (rotations / degrees)
        return angle * (54. / 15.) * (4. / 1.) * (3. / 1.) * (3. / 1.) * (1. / 360.);
    }

    private double rotationsToDegrees(double rotations) {
        // angle * sprocket ratio * net gear ratio * (degrees / rotations)
        return rotations * (15. / 54.) * (1. / 4.) * (1. / 3.) * (1. / 3.) * (360. / 1.);
    }

    private double absoluteEncoderToMotorRotations(double rotations) {
        return ((1.05 - rotations) % 1.0 - 0.05) * (4. / 1.) * (3. / 1.) * (3. / 1.);
    }
}
