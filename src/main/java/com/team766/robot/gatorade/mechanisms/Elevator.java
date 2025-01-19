package com.team766.robot.gatorade.mechanisms;

import static com.team766.robot.gatorade.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.ClosedLoopConfig.FeedbackSensor;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.logging.Severity;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Basic elevator mechanism.  Used in conjunction with the {@link Intake} and {@link Wrist}.
 * Can be moved up and down as part of teleop or autonomous control to move the {@link Wrist}
 * and {@link Intake} closer to a game piece or game element (eg node in the
 * field, human player station).
 */
public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    /**
     * @param height the current height of the elevator, in inches ('Murica).
     */
    public record ElevatorStatus(double rotations, double height) implements Status {
        public boolean isNearTo(Position position) {
            return isNearTo(position.getHeight());
        }

        public boolean isNearTo(double position) {
            return Math.abs(position - height) < NEAR_THRESHOLD;
        }
    }

    public enum Position {

        /** Elevator is fully retracted.  Starting position. */
        RETRACTED(0),
        /** Elevator is the appropriate height to place game pieces at the low node. */
        LOW(0),
        /** Elevator is the appropriate height to place game pieces at the mid node. */
        MID(18),
        /** Elevator is at appropriate height to place game pieces at the high node. */
        HIGH(40),
        /** Elevator is at appropriate height to grab cubes from the human player. */
        HUMAN_CUBES(39),
        /** Elevator is at appropriate height to grab cones from the human player. */
        HUMAN_CONES(40),
        /** Elevator is fully extended. */
        EXTENDED(40);

        private final double height;

        Position(double position) {
            this.height = position;
        }

        private double getHeight() {
            return height;
        }
    }

    private static final double NUDGE_INCREMENT = 2.0;
    private static final double NUDGE_DAMPENER = 0.25;

    private static final double NEAR_THRESHOLD = 2.0;

    private final SparkMax leftMotor;
    private final SparkMax rightMotor;
    private final SparkClosedLoopController pidController;
    private final ValueProvider<Double> pGain;
    private final ValueProvider<Double> iGain;
    private final ValueProvider<Double> dGain;
    private final ValueProvider<Double> ffGain;
    private final ValueProvider<Double> maxVelocity;
    private final ValueProvider<Double> minOutputVelocity;
    private final ValueProvider<Double> maxAccel;

    /**
     * Contructs a new Elevator.
     */
    public Elevator() {
        MotorController halLeftMotor = RobotProvider.instance.getMotor(ELEVATOR_LEFT_MOTOR);
        MotorController halRightMotor = RobotProvider.instance.getMotor(ELEVATOR_RIGHT_MOTOR);

        if (!((halLeftMotor instanceof SparkMax) && (halRightMotor instanceof SparkMax))) {
            log(Severity.ERROR, "Motors are not CANSparkMaxes!");
            throw new IllegalStateException("Motor are not CANSparkMaxes!");
        }

        halLeftMotor.setNeutralMode(NeutralMode.Brake);
        halRightMotor.setNeutralMode(NeutralMode.Brake);

        leftMotor = (SparkMax) halLeftMotor;
        rightMotor = (SparkMax) halRightMotor;

        SparkMaxConfig rightConfig = new SparkMaxConfig();
        rightConfig.follow(leftMotor, true /* invert */);
        rightMotor.configure(
                rightConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        leftMotor
                .getEncoder()
                .setPosition(
                        EncoderUtils.elevatorHeightToRotations(Position.RETRACTED.getHeight()));

        pidController = leftMotor.getClosedLoopController();

        SparkMaxConfig leftConfig = new SparkMaxConfig();
        leftConfig.closedLoop.feedbackSensor(FeedbackSensor.kPrimaryEncoder);
        leftMotor.configure(
                leftConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        pGain = ConfigFileReader.getInstance().getDouble(ELEVATOR_PGAIN);
        iGain = ConfigFileReader.getInstance().getDouble(ELEVATOR_IGAIN);
        dGain = ConfigFileReader.getInstance().getDouble(ELEVATOR_DGAIN);
        ffGain = ConfigFileReader.getInstance().getDouble(ELEVATOR_FFGAIN);
        maxVelocity = ConfigFileReader.getInstance().getDouble(ELEVATOR_MAX_VELOCITY);
        minOutputVelocity = ConfigFileReader.getInstance().getDouble(ELEVATOR_MIN_OUTPUT_VELOCITY);
        maxAccel = ConfigFileReader.getInstance().getDouble(ELEVATOR_MAX_ACCEL);
    }

    public void nudgeNoPID(double value) {
        checkContextReservation();
        double clampedValue = MathUtil.clamp(value, -1, 1);
        clampedValue *= NUDGE_DAMPENER; // make nudges less forceful.  TODO: make this non-linear
        leftMotor.set(clampedValue);
    }

    public void stopElevator() {
        checkContextReservation();
        leftMotor.set(0);
    }

    public void nudgeUp() {
        checkContextReservation();
        System.err.println("Nudging up.");

        double height = getStatus().height();
        // NOTE: this could artificially limit nudge range
        double targetHeight = Math.min(height + NUDGE_INCREMENT, Position.EXTENDED.getHeight());

        moveTo(targetHeight);
    }

    public void nudgeDown() {
        checkContextReservation();
        double height = getStatus().height();
        // NOTE: this could artificially limit nudge range
        double targetHeight = Math.max(height - NUDGE_INCREMENT, Position.RETRACTED.getHeight());
        moveTo(targetHeight);
    }

    /**
     * Moves the elevator to a pre-set {@link Position}.
     */
    public void moveTo(Position position) {
        checkContextReservation();
        moveTo(position.getHeight());
    }

    /**
     * Moves the elevator to a specific position (in inches).
     */
    public void moveTo(double position) {
        checkContextReservation();

        System.err.println("Setting target position to " + position);
        // set the PID controller values with whatever the latest is in the config
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.pid(pGain.get(), iGain.get(), dGain.get());
        config.closedLoop.outputRange(-0.4, 0.4);
        leftMotor.configure(
                config, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        double ff = ffGain.get();

        // pidController.setSmartMotionAccelStrategy(AccelStrategy.kTrapezoidal, 0);
        // pidController.setSmartMotionMaxVelocity(maxVelocity.get(), 0);
        // pidController.setSmartMotionMinOutputVelocity(minOutputVelocity.get(), 0);
        // pidController.setSmartMotionMaxAccel(maxAccel.get(), 0);

        // convert the desired target degrees to encoder units
        double rotations = EncoderUtils.elevatorHeightToRotations(position);

        // SmartDashboard.putNumber("[ELEVATOR] ff", ff);
        SmartDashboard.putNumber("[ELEVATOR] reference", rotations);

        // set the reference point for the wrist
        pidController.setReference(rotations, ControlType.kPosition, ClosedLoopSlot.kSlot0, ff);
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(
                leftMotor.getEncoder().getPosition(),
                EncoderUtils.elevatorRotationsToHeight(leftMotor.getEncoder().getPosition()));
    }
}
