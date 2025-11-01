package com.team766.robot.copy_2910.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import edu.wpi.first.math.MathUtil;

public class Shoulder extends MechanismWithStatus<Shoulder.ShoulderStatus> {

    private MotorController leftMotor;
    private MotorController rightMotor;
    private final EncoderReader absoluteEncoder;
    private boolean encoderInitialized = false;
    private double gearRatio = 120;
    private boolean noPIDMode;
    private final ValueProvider<Double> ffGain;

    private static final double THRESHOLD =
            0.5; // Threshold for determining if the shoulder is near a position | TODO: Adjust this
    // was 0.5
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
        L2(50.891),
        L3(70.247),
        L4(89),
        ALGAE_HIGH(22.952),
        ALGAE_LOW(20.405),
        CORAL_GROUND(1),
        ALGAE_GROUND(4.119),
        CLIMBER(100),
        STOW(0),
        MAXIMUM(100),
        MINIMUM(-10);

        private final double angle;

        ShoulderPosition(double angle) {
            this.angle = angle;
        }

        public double getPosition() {
            return angle;
        }
    }

    public Shoulder() {
        noPIDMode = false;
        leftMotor =
                RobotProvider.instance.getMotor(
                        "LeftShoulderMotor"); // Replace with actual motor name
        rightMotor =
                RobotProvider.instance.getMotor(
                        "RightShoulderMotor"); // Replace with actual motor name
        absoluteEncoder =
                RobotProvider.instance.getEncoder(
                        "ShoulderEncoder"); // **ShoulderEncoder may not exist**
        leftMotor.setNeutralMode(NeutralMode.Coast);
        rightMotor.setNeutralMode(NeutralMode.Coast);
        // leftMotor.setInverted(true);
        // leftMotor.setInverted(true);
        rightMotor.follow(leftMotor);

        leftMotor.setCurrentLimit(40);
        rightMotor.setCurrentLimit(40);
        ffGain = ConfigFileReader.getInstance().getDouble("Shoulder_FFGain");
        // ffGain =
        //      ConfigFileReader.instance.getDouble(
        //            "ShoulderFFGain"); // Replace with actual config key
        // setPoint = ShoulderPosition.L1.getPosition(); // Default position
    }

    public void setSetpoint(double setpoint) {
        noPIDMode = false;
        setPoint =
                MathUtil.clamp(
                        setpoint,
                        ShoulderPosition.MINIMUM.getPosition(),
                        ShoulderPosition.MAXIMUM.getPosition());
    }

    public void setPosition(ShoulderPosition position) {
        setSetpoint(position.getPosition());
    }

    public void setBrakeMode() {
        leftMotor.setNeutralMode(NeutralMode.Brake);
        rightMotor.setNeutralMode(NeutralMode.Brake);
    }

    public void run() {
        // leftMotor.set(.Position, setPoint)
        // All of the following PID code is directly copied from the 2910 wrist code -> may not work
        // properly
        if (!noPIDMode) {
            double ff =
                    ffGain.valueOr(0.0); // * Math.cos(Math.toRadians(getStatus().currentAngle()));
            leftMotor.set(MotorController.ControlMode.Position, setPoint, ff);
        } else {
            leftMotor.set(MotorController.ControlMode.Position, setPoint);
        }
        log("SHOULDER Setpoint: " + setPoint + " Pos: " + leftMotor.getSensorPosition());
    }

    public void nudgeUp() {
        setPoint += 1;
    }

    public void nudgeDown() {
        setSetpoint(setPoint - NUDGE_AMOUNT);
    }

    public void nudge(double input) {
        noPIDMode = false;
        if (input > 0) {
            nudgeUp();
        } else {
            nudgeDown();
        }
    }

    public void nudgeNoPID(double input) {
        noPIDMode = true;
        if (input > 0) {
            nudgeUp();
        } else {
            nudgeDown();
        }
    }

    @Override
    protected ShoulderStatus updateStatus() {
        if (!encoderInitialized && absoluteEncoder.isConnected()) {
            double motorRotations = absoluteEncoder.getPosition() * gearRatio;
            leftMotor.setSensorPosition(motorRotations);
            encoderInitialized = true;
        }
        return new ShoulderStatus(leftMotor.getSensorPosition(), setPoint);
    }
}
