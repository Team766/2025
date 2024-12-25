package com.team766.robot.gatorade.mechanisms;

import static com.team766.robot.gatorade.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;
import com.team766.library.ValueProvider;
import edu.wpi.first.math.MathUtil;

/**
 * Basic elevator mechanism.  Used in conjunction with the {@link Intake} and {@link Wrist}.
 * Can be moved up and down as part of teleop or autonomous control to move the {@link Wrist}
 * and {@link Intake} closer to a game piece or game element (eg node in the
 * field, human player station).
 */
public class Elevator extends Mechanism<Elevator.ElevatorStatus> {
    public static class Position {
        /** Elevator is fully retracted.  Starting position. */
        public static final double RETRACTED = 0;

        /** Elevator is the appropriate height to place game pieces at the low node. */
        public static final double LOW = 0;

        /** Elevator is the appropriate height to place game pieces at the mid node. */
        public static final double MID = 18;

        /** Elevator is at appropriate height to place game pieces at the high node. */
        public static final double HIGH = 40;

        /** Elevator is at appropriate height to grab cubes from the human player. */
        public static final double HUMAN_CUBES = 39;

        /** Elevator is at appropriate height to grab cones from the human player. */
        public static final double HUMAN_CONES = 40;

        /** Elevator is fully extended. */
        public static final double EXTENDED = 40;
    }

    /**
     * @param height the current height of the elevator, in inches ('Murica).
     */
    public record ElevatorStatus(double rotations, double height) implements Status {
        public boolean isNearTo(double position) {
            return Math.abs(position - height) < NEAR_THRESHOLD;
        }
    }

    private static final double NUDGE_INCREMENT = 2.0;
    private static final double NUDGE_DAMPENER = 0.25;

    private static final double NEAR_THRESHOLD = 2.0; // inches
    private static final double STOPPED_VELOCITY_THRESHOLD = 1.0; // inches/sec

    private final CANSparkMaxMotorController leftMotor;
    private final CANSparkMaxMotorController rightMotor;
    private final ValueProvider<Double> ffGain;

    /**
     * Contructs a new Elevator.
     */
    public Elevator() {
        leftMotor =
                (CANSparkMaxMotorController) RobotProvider.instance.getMotor(ELEVATOR_LEFT_MOTOR);
        rightMotor =
                (CANSparkMaxMotorController) RobotProvider.instance.getMotor(ELEVATOR_RIGHT_MOTOR);

        leftMotor.setNeutralMode(NeutralMode.Brake);
        rightMotor.setNeutralMode(NeutralMode.Brake);

        rightMotor.follow(leftMotor, true /* invert */);

        leftMotor.setSensorPosition(EncoderUtils.elevatorHeightToRotations(Position.RETRACTED));

        leftMotor.setOutputRange(-0.4, 0.4);

        ffGain = ConfigFileReader.getInstance().getDouble(ELEVATOR_FFGAIN);
    }

    public Request<Elevator> requestStop() {
        return setRequest(leftMotor.requestStop());
    }

    public Request<Elevator> requestNudgeNoPID(double value) {
        double clampedValue = MathUtil.clamp(value, -1, 1);
        clampedValue *= NUDGE_DAMPENER; // make nudges less forceful.  TODO: make this non-linear
        return setRequest(leftMotor.requestPercentOutput(clampedValue));
    }

    public Request<Elevator> requestHoldPosition() {
        final double currentHeight = getStatus().height();
        return requestPosition(currentHeight);
    }

    public Request<Elevator> requestNudgeUp() {
        final double currentHeight = getStatus().height();
        // NOTE: this could artificially limit nudge range
        final double targetHeight = Math.min(currentHeight + NUDGE_INCREMENT, Position.EXTENDED);
        return requestPosition(targetHeight);
    }

    public Request<Elevator> requestNudgeDown() {
        final double currentHeight = getStatus().height();
        // NOTE: this could artificially limit nudge range
        final double targetHeight = Math.max(currentHeight - NUDGE_INCREMENT, Position.RETRACTED);
        return requestPosition(targetHeight);
    }

    /**
     * Moves the elevator to a specific position (in inches).
     */
    public Request<Elevator> requestPosition(double targetHeight) {
        final double ff = ffGain.get();

        // convert the desired target degrees to encoder units
        return setRequest(
                leftMotor.requestPosition(
                        EncoderUtils.elevatorHeightToRotations(targetHeight),
                        EncoderUtils.elevatorHeightToRotations(NEAR_THRESHOLD),
                        EncoderUtils.elevatorHeightToRotations(STOPPED_VELOCITY_THRESHOLD)
                                * 60, // RPMs
                        ff));
    }

    @Override
    protected ElevatorStatus reportStatus() {
        return new ElevatorStatus(
                leftMotor.getEncoder().getPosition(),
                EncoderUtils.elevatorRotationsToHeight(leftMotor.getEncoder().getPosition()));
    }
}
