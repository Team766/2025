package com.team766.robot.reva_2025.mechanisms;

import static com.team766.robot.reva_2025.constants.ConfigConstants.WRIST_ENCODER;
import static com.team766.robot.reva_2025.constants.ConfigConstants.WRIST_GYRO;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.PigeonGyro;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private double setPoint;
    private static final double NUDGE_AMOUNT = 5.0;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private final ValueProvider<Double> ffGain;
    private boolean noPIDMode;
    private final EncoderReader absoluteEncoder;
    private final Pigeon2 gyro;
    private boolean encoderInitialized = false;

    public record WristStatus(double currentAngle, double targetAngle) implements Status {
        public boolean isAtAngle() {
            return Math.abs(targetAngle() - currentAngle()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum WristPosition {

        // TODO: Change these angles to actual values
        CORAL_BOTTOM(30),
        CORAL_START(30),
        CORAL_INTAKE(40),
        // CORAL_L2_PREP(260),
        CORAL_L1_PLACE(40),
        CORAL_L2_PLACE(245),
        // CORAL_L3_PREP(220),
        CORAL_L3_PLACE(215),
        // CORAL_L4_PREP(210),
        CORAL_L4_PLACE(215),
        CORAL_TOP(300),
        CORAL_CLIMB(300);

        private final double angle;

        private WristPosition(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    private MotorController wristMotor;

    public Wrist() {
        wristMotor = RobotProvider.instance.getMotor(ConfigConstants.WRIST_MOTOR);
        ffGain = ConfigFileReader.getInstance().getDouble(ConfigConstants.WRIST_FFGAIN);
        setPoint = WristPosition.CORAL_START.getAngle();
        noPIDMode = false;
        absoluteEncoder = RobotProvider.instance.getEncoder(WRIST_ENCODER);
        gyro = ((PigeonGyro) RobotProvider.instance.getGyro(WRIST_GYRO)).getPigeon();
        wristMotor.setCurrentLimit(30);
    }

    public void setAngle(WristPosition position) {
        setAngle(position.getAngle());
    }

    public void setAngle(double angle) {
        noPIDMode = false;
        setPoint = angle;
    }

    public void nudge(double sign) {
        setPoint = getStatus().currentAngle() + (NUDGE_AMOUNT * Math.signum(sign));
    }

    public void nudgeNoPID(double power) {
        noPIDMode = true;
        wristMotor.set(power);
    }

    @Override
    protected void run() {
        if (!noPIDMode) {
            double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().currentAngle()));
            wristMotor.set(
                    MotorController.ControlMode.Position,
                    EncoderUtils.coralWristDegreesToRotations(setPoint),
                    ff);
        }
    }

    @Override
    protected WristStatus updateStatus() {
        if (!encoderInitialized && absoluteEncoder.isConnected()) {
            double encoderPos = absoluteEncoder.getPosition();
            double gyroReading = // 0;
                    Math.toDegrees(
                            Math.atan2(
                                    -gyro.getGravityVectorZ().getValueAsDouble(),
                                    -gyro.getGravityVectorY().getValueAsDouble()));
            double convertedPos =
                    gyroReading
                            + Math.IEEEremainder(
                                    encoderPos * (14. / 48.) * 360. - gyroReading,
                                    EncoderUtils.CORAL_WRIST_ABSOLUTE_ENCODER_RANGE);
            wristMotor.setSensorPosition(EncoderUtils.coralWristDegreesToRotations(convertedPos));
            encoderInitialized = true;
        }
        return new WristStatus(
                EncoderUtils.coralWristRotationsToDegrees(wristMotor.getSensorPosition()),
                setPoint);
    }
}
