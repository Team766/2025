package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import edu.wpi.first.math.MathUtil;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private MotorController motor;

    private static final double THRESHOLD =
            0.5; // Threshold for determining if the wrist is near a position | TODO: Adjust this
    // value based on the wrist's characteristics
    private double setPoint;
    // private ValueProvider<Double> ffGain;

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
        L1(-16.643),
        L2(0.643),
        L3(-1.667),
        L4(-7),
        ALGAE_HIGH(-25.928),
        ALGAE_LOW(-25.928),
        CORAL_GROUND(-15.2),
        ALGAE_GROUND(-21.786),
        MAXIMUM(10),
        MINIMUM(-30);

        private final double angle;

        WristPosition(double angle) {
            this.angle = angle;
        }

        public double getPosition() {
            return angle;
        }
    }

    public Wrist() {
        motor = RobotProvider.instance.getMotor("WristMotor"); // Replace with actual motor name

        // ffGain =
        //        ConfigFileReader.instance.getDouble(
        //                "WristFFGain"); // Replace with actual config key
        setPoint = WristPosition.L3.getPosition(); // Default position
    }

    public void setSetpoint(double setpoint) {
        setPoint =
                MathUtil.clamp(
                        setpoint,
                        WristPosition.MINIMUM.getPosition(),
                        WristPosition.MAXIMUM.getPosition());
    }

    public void run() {
        motor.set(MotorController.ControlMode.Position, setPoint);
    }

    public void nudgeUp() {
        setSetpoint(setPoint + NUDGE_AMOUNT);
    }

    public void nudgeDown() {
        setSetpoint(setPoint - NUDGE_AMOUNT);
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(motor.getSensorPosition(), setPoint);
    }
}
