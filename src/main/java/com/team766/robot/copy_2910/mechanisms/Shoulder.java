package com.team766.robot.copy_2910.mechanisms;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import edu.wpi.first.math.MathUtil;

public class Shoulder extends MechanismWithStatus<Shoulder.ShoulderStatus> {

    private MotorController leftMotor;
    private MotorController rightMotor;

    private static final double THRESHOLD =
            0.5; // Threshold for determining if the shoulder is near a position | TODO: Adjust this
    // value based on the shoulder's characteristics
    private double setPoint;
    // private ValueProvider<Double> ffGain;

    private final double NUDGE_AMOUNT =
            0.5; // Amount to nudge up/down | TODO: Adjust this value based on the shoulder's

    // characteristics

    public record ShoulderStatus(double position, double setPoint) implements Status {
        public boolean isNearTo(ShoulderPosition position) {
            return isNearTo(position.getPosition());
        }

        public boolean isNearTo(double angle) {
            return Math.abs(angle - this.position()) < THRESHOLD;
        }
    }

    public enum ShoulderPosition {
        L1(14),
        L2(11.5),
        L3(17),
        L4(24.5),
        ALGAE_HIGH(22.952),
        ALGAE_LOW(20.405),
        CORAL_GROUND(-0.75),
        ALGAE_GROUND(4.119),
        CLIMBER(28),
        STOW(0),
        MAXIMUM(40),
        MINIMUM(-1);

        private final double angle;

        ShoulderPosition(double angle) {
            this.angle = angle;
        }

        public double getPosition() {
            return angle;
        }
    }

    public Shoulder() {
        leftMotor =
                RobotProvider.instance.getMotor(
                        "LeftShoulderMotor"); // Replace with actual motor name
        rightMotor =
                RobotProvider.instance.getMotor(
                        "RightShoulderMotor"); // Replace with actual motor name

        //leftMotor.setInverted(true);
        rightMotor.follow(leftMotor);

        leftMotor.setCurrentLimit(40);
        rightMotor.setCurrentLimit(40);

        // ffGain =
        //      ConfigFileReader.instance.getDouble(
        //            "ShoulderFFGain"); // Replace with actual config key
        // setPoint = ShoulderPosition.L1.getPosition(); // Default position
    }

    public void setSetpoint(double setpoint) {
        setPoint =
                MathUtil.clamp(
                        setpoint,
                        ShoulderPosition.MINIMUM.getPosition(),
                        ShoulderPosition.MAXIMUM.getPosition());
    }

    public void setPosition(ShoulderPosition position) {
        setSetpoint(position.getPosition());
    }

    public void run() {
        // leftMotor.set(.Position, setPoint)
        leftMotor.set(MotorController.ControlMode.Position, setPoint);
        log("SHOULDER Setpoint: " + setPoint + " Pos: " + leftMotor.getSensorPosition());
    }

    public void nudgeUp() {
        setPoint += 1;
    }

    public void nudgeDown() {
        setSetpoint(setPoint - NUDGE_AMOUNT);
    }

    public void nudge(double input) {
        if (input > 0) {
            nudgeUp();
        } else {
            nudgeDown();
        }
    }

    @Override
    protected ShoulderStatus updateStatus() {
        return new ShoulderStatus(leftMotor.getSensorPosition(), setPoint);
    }
}
