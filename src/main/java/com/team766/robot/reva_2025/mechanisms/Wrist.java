package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private double setPoint;
    private static final double NUDGE_AMOUNT = 1.0;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private final ValueProvider<Double> ffGain;

    public record WristStatus(double currentAngle, double targetAngle) implements Status {
        public boolean isAtAngle() {
            return Math.abs(targetAngle() - currentAngle()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum WristPosition {

        // TODO: Change these angles to actual values
        CORAL_BOTTOM(10),
        CORAL_START(35),
        CORAL_INTAKE(40),
        // CORAL_L2_PREP(260),
        CORAL_L1_PLACE(40),
        CORAL_L2_PLACE(210),
        // CORAL_L3_PREP(220),
        CORAL_L3_PLACE(210),
        // CORAL_L4_PREP(210),
        CORAL_L4_PLACE(195),
        CORAL_TOP(300);

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
        wristMotor.setSensorPosition(EncoderUtils.coralWristDegreesToRotations(setPoint));
    }

    public void setAngle(WristPosition position) {
        setAngle(position.getAngle());
    }

    public void setAngle(double angle) {
        setPoint = angle;
    }

    @Override
    protected void run() {
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().currentAngle()));
        wristMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.coralWristDegreesToRotations(setPoint),
                ff);
    }

    public void nudge(double sign) {
        setPoint = getStatus().currentAngle() + (NUDGE_AMOUNT * Math.signum(sign));
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(
                EncoderUtils.coralWristRotationsToDegrees(wristMotor.getSensorPosition()), setPoint);
    }
}
