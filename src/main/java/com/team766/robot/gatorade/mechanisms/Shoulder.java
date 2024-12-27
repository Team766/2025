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
 * Basic shoulder mechanism.  Rotates the {@link Elevator} to different angles, to allow it (and the
 * attached {@link Wrist} and {@link Intake}) to reach different positions, from the floor to different
 * heights of nodes.
 */
public class Shoulder extends MechanismWithStatus<Shoulder.ShoulderStatus> {
    public static class Position {
        // TODO: adjust these!

        /** Shoulder is at the highest achievable position. */
        public static final double TOP = 45;

        /** Shoulder is in position to intake from the substation or score in the upper nodes. */
        public static final double RAISED = 40;

        /** Shoulder is in position to intake and outtake pieces from/to the floor. */
        public static final double FLOOR = 10;

        /** Shoulder is fully down.  Starting position. **/
        public static final double BOTTOM = 0;
    }

    /**
     * @param angle the current angle of the wrist.
     */
    public record ShoulderStatus(double rotations, double angle) implements Status {
        public boolean isNearTo(double angle) {
            return Math.abs(angle - this.angle()) < NEAR_THRESHOLD;
        }
    }

    private static final double NUDGE_INCREMENT = 5.0;
    private static final double NUDGE_DAMPENER = 0.15;

    private static final double NEAR_THRESHOLD = 5.0; // degrees
    private static final double STOPPED_VELOCITY_THRESHOLD = 5.0; // degrees/sec

    private final CANSparkMaxMotorController leftMotor;
    private final CANSparkMaxMotorController rightMotor;
    private final ValueProvider<Double> ffGain;

    /**
     * Constructs a new Shoulder.
     */
    public Shoulder() {
        leftMotor =
                (CANSparkMaxMotorController) RobotProvider.instance.getMotor(SHOULDER_LEFT_MOTOR);
        rightMotor =
                (CANSparkMaxMotorController) RobotProvider.instance.getMotor(SHOULDER_RIGHT_MOTOR);

        leftMotor.setNeutralMode(NeutralMode.Brake);
        rightMotor.setNeutralMode(NeutralMode.Brake);

        rightMotor.follow(leftMotor, true /* invert */);

        leftMotor.setSensorPosition(EncoderUtils.shoulderDegreesToRotations(Position.BOTTOM));

        leftMotor.setOutputRange(-0.4, 0.4);

        ffGain = ConfigFileReader.getInstance().getDouble(SHOULDER_FFGAIN);
    }

    public Request<Shoulder> requestNudgeNoPID(double value) {
        double clampedValue = MathUtil.clamp(value, -1, 1);
        clampedValue *= NUDGE_DAMPENER; // make nudges less forceful. TODO: make this non-linear
        return startRequest(leftMotor.requestPercentOutput(clampedValue));
    }

    public Request<Shoulder> requestStop() {
        return startRequest(leftMotor.requestStop());
    }

    public Request<Shoulder> requestHoldPosition() {
        final double currentAngle = getStatus().angle();
        return requestPosition(currentAngle);
    }

    public Request<Shoulder> requestNudgeUp() {
        final double currentAngle = getStatus().angle();
        final double targetAngle = Math.min(currentAngle + NUDGE_INCREMENT, Position.TOP);
        return requestPosition(targetAngle);
    }

    public Request<Shoulder> requestNudgeDown() {
        final double currentAngle = getStatus().angle();
        final double targetAngle = Math.max(currentAngle - NUDGE_INCREMENT, Position.BOTTOM);
        return requestPosition(targetAngle);
    }

    /**
     * Rotates the wrist to the specified angle (in degrees).
     */
    public Request<Shoulder> requestPosition(double targetAngle) {
        final double ff = ffGain.get() * Math.cos(Math.toRadians(targetAngle));

        // convert the desired target degrees to rotations
        return startRequest(
                leftMotor.requestPosition(
                        EncoderUtils.shoulderDegreesToRotations(targetAngle),
                        EncoderUtils.shoulderDegreesToRotations(NEAR_THRESHOLD),
                        EncoderUtils.shoulderDegreesToRotations(STOPPED_VELOCITY_THRESHOLD)
                                * 60, // RPMs
                        ff));
    }

    @Override
    protected ShoulderStatus reportStatus() {
        return new ShoulderStatus(
                leftMotor.getSensorPosition(),
                EncoderUtils.shoulderRotationsToDegrees(leftMotor.getSensorPosition()));
    }
}
