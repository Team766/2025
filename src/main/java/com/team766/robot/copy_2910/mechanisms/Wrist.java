package com.team766.robot.copy_2910.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import edu.wpi.first.math.MathUtil;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private MotorController motor;
    private double gearRatio = 21.8;
    private final ValueProvider<Double> ffGain;
    private boolean noPIDMode;
    private final EncoderReader absoluteEncoder;
    private boolean encoderInitialized = false;

    private static final double THRESHOLD =
            0.5; // Threshold for determining if the wrist is near a position | TODO: Adjust this
    // value based on the wrist's characteristics
    private double setPoint;
    // private ValueProvider<Double> ffGain;

    private final double NUDGE_AMOUNT =
            0.5; // Amount to nudge up/down | TODO: Adjust this value based on the wrist's

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
        L1(-16.643), // 16.643
        L2(1.095),
        L3(-0.808), // -0.808
        L4(-5.561),
        ALGAE_HIGH(-25.928),
        ALGAE_LOW(-25.928),
        CORAL_GROUND(-9.75),
        ALGAE(-25.2),
        ALGAE_GROUND(-21.786),
        ALGAE_SHOOT(0.643),
        STOW(0),
        MAXIMUM(50),
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
        ffGain =
                ConfigFileReader.getInstance()
                        .getDouble("Wrist_FFGain"); // ** Wrist_FFGain does not exist yet **
        noPIDMode = true;
        absoluteEncoder =
                RobotProvider.instance.getEncoder(
                        "WristEncoder"); // ** WristEncoder may not exist **
        motor.setCurrentLimit(40);

        // ffGain =
        //        ConfigFileReader.instance.getDouble(
        //                "WristFFGain"); // Replace with actual config key
        // setPoint = WristPosition.L3.getPosition(); // Default position
    }

    public void setSetpoint(double setpoint) {
        noPIDMode = true;
        setPoint =
                MathUtil.clamp(
                        setpoint,
                        WristPosition.MINIMUM.getPosition(),
                        WristPosition.MAXIMUM.getPosition());
    }

    public void setPosition(WristPosition wristPosition) {
        setSetpoint(wristPosition.getPosition());
    }

    public void run() {
        if (!noPIDMode) {
            double ff =
                    ffGain.valueOr(0.0); // * Math.cos(Math.toRadians(getStatus().currentAngle()));
            motor.set(MotorController.ControlMode.Position, setPoint / gearRatio, ff);
            /*wristMotor.set(
            MotorController.ControlMode.Position,
            EncoderUtils.coralWristDegreesToRotations(setPoint),
            ff); */
        } else {
            motor.set(MotorController.ControlMode.Position, setPoint);
        }
    }

    public void nudgeUp() {
        setPoint += NUDGE_AMOUNT;
    }

    public void nudgeDown() {
        setPoint -= NUDGE_AMOUNT;
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
    protected WristStatus updateStatus() {
        if (!encoderInitialized && absoluteEncoder.isConnected()) {
            double motorRotations = absoluteEncoder.getPosition() * gearRatio;
            motor.setSensorPosition(motorRotations);
            encoderInitialized = true;
        }
        return new WristStatus(motor.getSensorPosition(), setPoint);
    }
}
