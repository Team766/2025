package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.EncoderUtils;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private MotorController wristMotor;

    private ValueProvider<Double> ffGain;

    private static final double NUDGE_AMOUNT = 5;
    private static final double THRESHOLD_CONSTANT = 0; // TODO: Update me after testing!

    // values are untested and are set to

    public record WristStatus(double currentAngle) implements Status {
        public boolean isAtPosition(double target) {
            return (Math.abs(target - currentAngle()) < THRESHOLD_CONSTANT);
        }
    }

    public enum Position {
        WRIST_BOTTOM(0),
        WRIST_TOP(180);

        private double angle;

        Position(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }

        public double getMotorRotations() {
            return EncoderUtils.coralWristDegreesToRotations(angle);
        }
    }

    public Wrist() {
        wristMotor = RobotProvider.instance.getMotor("wrist.motor");
        ffGain = ConfigFileReader.getInstance().getDouble("wrist.motor.ffGain");
        wristMotor.setSensorPosition(0);
    }

    /**
     *
     * @param setPosition in degrees
     */
    public void setPosition(double setPosition) {
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(setPosition));
        wristMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.coralWristDegreesToRotations(setPosition),
                ff);
    }

    public void nudgeNoPID(double value) {
        wristMotor.set(value);
    }

    public void setPosition(Position position) {
        setPosition(position.getMotorRotations());
    }

    public void nudgeUp() {
        double nudgePosition = getStatus().currentAngle() + NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void nudgeDown() {
        double nudgePosition = getStatus().currentAngle() - NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(
                EncoderUtils.coralWristRotationsToDegrees(wristMotor.getSensorPosition()));
    }
}
