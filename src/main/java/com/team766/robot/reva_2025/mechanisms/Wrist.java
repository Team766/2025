package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private final double thresholdConstant = 0; // TODO: Update me after testing!
    private double setPoint;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private final ValueProvider<Double> ffGain;

    public record WristStatus(double currentAngle, double targetAngle) implements Status {
        public boolean isAtAngle() {
            return Math.abs(targetAngle() - currentAngle()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum WristPosition {

        // TODO: update all of these to real things!
        PICKUP_CORAL(0),
        PLACE_CORAL(180);

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

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(
                EncoderUtils.coralWristRotationsToDegrees(wristMotor.get()), setPoint);
    }
}
