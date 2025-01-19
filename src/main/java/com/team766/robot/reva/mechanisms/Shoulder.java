package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_ENCODER;
import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_LEFT;
import static com.team766.robot.reva.constants.ConfigConstants.SHOULDER_RIGHT;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix6.controls.PositionDutyCycle;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.REVThroughBoreDutyCycleEncoder;
import com.team766.library.ValueProvider;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shoulder extends MechanismWithStatus<Shoulder.ShoulderStatus> {
    public record ShoulderStatus(double angle) implements Status {
        public boolean isNearTo(ShoulderPosition target) {
            return isNearTo(target.getAngle());
        }

        public boolean isNearTo(double targetAngle) {
            return Math.abs(angle() - targetAngle) < 2.5;
        }
    }

    public enum ShoulderPosition {
        // TODO: Find actual values.
        BOTTOM(0),
        INTAKE_FLOOR(0),
        SHOOT_LOW(15),
        SHOOTER_ASSIST(18.339),
        SHOOT_MEDIUM(30),
        SHOOT_HIGH(80),
        AMP(90),
        TOP(105); // angle needed to be upped so it works with the climber

        private final double angle;

        ShoulderPosition(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    private static final double NUDGE_AMOUNT = 1; // degrees

    private final REVThroughBoreDutyCycleEncoder absoluteEncoder;
    private boolean encoderInitialized = false;
    private static final double SUPPLY_CURRENT_LIMIT = 30.0; // max efficiency from spec sheet
    private static final double STATOR_CURRENT_LIMIT = 80.0; // TUNE THIS!
    private static final double DEFAULT_POSITION = 77.0;

    private MotorController leftMotor;
    private MotorController rightMotor;

    private ValueProvider<Double> ffGain;
    private double targetRotations = 0.0;

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

    public void stop() {
        leftMotor.stopMotor();
    }

    public void reset() {
        checkContextReservation();
        targetRotations = 0.0;
        leftMotor.setSensorPosition(0.0);
    }

    public void nudgeUp() {
        rotate(getStatus().angle() + NUDGE_AMOUNT);
    }

    public void nudgeDown() {
        rotate(getStatus().angle() - NUDGE_AMOUNT);
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

    public void rotate(ShoulderPosition position) {
        rotate(position.getAngle());
    }

    public void rotate(double angle) {
        checkContextReservation();
        double targetAngle =
                com.team766.math.Math.clamp(
                        angle, ShoulderPosition.BOTTOM.getAngle(), ShoulderPosition.TOP.getAngle());
        targetRotations = degreesToRotations(targetAngle);
        // SmartDashboard.putNumber("[SHOULDER Target Angle]", targetAngle);
        // actual rotation will happen in run()
    }

    @Override
    public void run() {
        TalonFX leftTalon = (TalonFX) leftMotor;
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().angle()));
        PositionDutyCycle positionRequest = new PositionDutyCycle(targetRotations);
        positionRequest.FeedForward = ff;
        leftTalon.setControl(positionRequest);
    }

    @Override
    protected ShoulderStatus updateStatus() {
        // encoder takes some time to settle.
        // this threshold was determined very scientifically around 3:20am.
        if (!encoderInitialized && absoluteEncoder.isConnected()) {
            double absPos = absoluteEncoder.get() - 0.071;
            double convertedPos = absoluteEncoderToMotorRotations(absPos);
            leftMotor.setSensorPosition(convertedPos);
            encoderInitialized = true;
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
        SmartDashboard.putNumber("[SHOULDER] Angle", angle);
        SmartDashboard.putNumber("[SHOULDER] Target Angle", rotationsToDegrees(targetRotations));

        return new ShoulderStatus(angle);
    }
}
