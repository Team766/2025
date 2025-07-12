package com.team766.robot.copy_2910.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import edu.wpi.first.math.MathUtil;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private MotorController motor;

    private static final double THRESHOLD =
            0.5; // Threshold for determining if the wrist is near a position | TODO: Adjust this
    // value based on the wrist's characteristics
    private double setPoint;
    private ValueProvider<Double> ffGain;

    private final double NUDGE_AMOUNT =
            5; // Amount to nudge up/down | TODO: Adjust this value based on the wrist's

    // characteristics

    public record WristStatus(double position, double setPoint) implements Status {
        public boolean isNearTo(WristPosition position) {
            return isNearTo(position.getPosition());
        }

        public boolean isNearTo(double angle) {
            return Math.abs(angle - this.position()) < THRESHOLD;
        }
    }

    public enum WristPosition {
        TOP(45),
        RAISED(40),
        FLOOR(10),
        READY(5),
        MAXIMUM(50),
        MINIMUM(0),
        BOTTOM(0);

        private final double angle;

        WristPosition(double angle) {
            this.angle = angle;
        }

        public double getPosition() {
            return angle;
        }
    }

    public Wrist() {
        motor =
                RobotProvider.instance.getMotor(
                        "WristMotor"); // Replace with actual motor name

        ffGain =
                ConfigFileReader.instance.getDouble(
                        "ShoulderFFGain"); // Replace with actual config key
        setPoint = WristPosition.READY.getPosition(); // Default position
    }

    public void setSetpoint(double setpoint) {
        setPoint =
                MathUtil.clamp(
                        setpoint,
                        WristPosition.MINIMUM.getPosition(),
                        WristPosition.MAXIMUM.getPosition());
    }

    public void run() {
        motor.set(MotorController.ControlMode.Position, setPoint, ffGain.get());
    }

    public void nudgeUp() {
        setPoint += NUDGE_AMOUNT;
        setPoint =
                MathUtil.clamp(
                        setPoint,
                        WristPosition.MINIMUM.getPosition(),
                        WristPosition.MAXIMUM.getPosition());
    }

    public void nudgeDown() {
        setPoint -= NUDGE_AMOUNT;
        setPoint =
                MathUtil.clamp(
                        setPoint,
                        WristPosition.MINIMUM.getPosition(),
                        WristPosition.MAXIMUM.getPosition());
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(motor.getSensorPosition(), setPoint);
    }
}