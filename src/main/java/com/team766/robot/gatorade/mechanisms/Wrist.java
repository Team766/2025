package com.team766.robot.gatorade.mechanisms;

import static com.team766.robot.gatorade.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;
import com.team766.library.ValueProvider;
import edu.wpi.first.math.MathUtil;

/**
 * Basic wrist mechanism.  Used in conjunction with the {@link Intake} and {@link Elevator}.
 * Can be moved up and down as part of teleop or autonomous control to move the {@link Intake}
 * (attached to the end of the Wrist) closer to a game piece or game element (eg node in the
 * field, human player station), at which point the {@link Intake} can grab or release the game
 * piece as appropriate.
 */
public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {
    public static class Position {
        /** Wrist is in top position.  Starting position. */
        public static final double TOP = -180;

        /** Wrist is in the position for moving around the field. */
        public static final double RETRACTED = -175.0;

        /** Wrist is level with ground. */
        public static final double LEVEL = -65;

        public static final double HIGH_NODE = -20;
        public static final double MID_NODE = -25.5;
        public static final double HUMAN_CONES = -4;
        public static final double HUMAN_CUBES = -8;

        /** Wrist is fully down. **/
        public static final double BOTTOM = 60;
    }

    /**
     * @param angle the current angle of the wrist.
     */
    public record WristStatus(double rotations, double angle) implements Status {
        public boolean isNearTo(double angle) {
            return Math.abs(angle - this.angle()) < NEAR_THRESHOLD;
        }
    }

    private static final double NUDGE_INCREMENT = 5.0;
    private static final double NUDGE_DAMPENER = 0.15;

    private static final double NEAR_THRESHOLD = 5.0; // degrees
    private static final double STOPPED_VELOCITY_THRESHOLD = 5.0; // degrees/sec

    private final CANSparkMaxMotorController motor;
    private final ValueProvider<Double> ffGain;

    /**
     * Contructs a new Wrist.
     */
    public Wrist() {
        motor = (CANSparkMaxMotorController) RobotProvider.instance.getMotor(WRIST_MOTOR);

        motor.setNeutralMode(NeutralMode.Brake);

        motor.setSensorPosition(EncoderUtils.wristDegreesToRotations(Position.TOP));

        motor.setOutputRange(-1, 1);

        ffGain = ConfigFileReader.getInstance().getDouble(WRIST_FFGAIN);
    }

    public Request<Wrist> requestNudgeNoPID(double value) {
        double clampedValue = MathUtil.clamp(value, -1, 1);
        clampedValue *= NUDGE_DAMPENER; // make nudges less forceful. TODO: make this non-linear
        return startRequest(motor.requestPercentOutput(clampedValue));
    }

    public Request<Wrist> requestStop() {
        return startRequest(motor.requestStop());
    }

    public Request<Wrist> requestHoldPosition() {
        final double currentAngle = getStatus().angle();
        return requestPosition(currentAngle);
    }

    public Request<Wrist> requestNudgeUp() {
        final double currentAngle = getStatus().angle();
        final double targetAngle = Math.max(currentAngle - NUDGE_INCREMENT, Position.TOP);
        return requestPosition(targetAngle);
    }

    public Request<Wrist> requestNudgeDown() {
        final double currentAngle = getStatus().angle();
        final double targetAngle = Math.min(currentAngle + NUDGE_INCREMENT, Position.BOTTOM);
        return requestPosition(targetAngle);
    }

    /**
     * Starts rotating the wrist to the specified angle.
     */
    public Request<Wrist> requestPosition(double targetAngle) {
        double ff = ffGain.get() * Math.cos(Math.toRadians(targetAngle));

        // convert the desired target degrees to rotations
        return startRequest(
                motor.requestPosition(
                        EncoderUtils.wristDegreesToRotations(targetAngle),
                        EncoderUtils.wristDegreesToRotations(NEAR_THRESHOLD),
                        EncoderUtils.wristDegreesToRotations(STOPPED_VELOCITY_THRESHOLD)
                                * 60, // RPMs
                        ff));
    }

    @Override
    protected WristStatus reportStatus() {
        return new WristStatus(
                motor.getSensorPosition(),
                EncoderUtils.wristRotationsToDegrees(motor.getSensorPosition()));
    }
}
