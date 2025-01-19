package com.team766.robot.gatorade.mechanisms;

import static com.team766.robot.gatorade.constants.ConfigConstants.*;

import com.revrobotics.spark.ClosedLoopSlot;
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
 * Basic wrist mechanism.  Used in conjunction with the {@link Intake} and {@link Elevator}.
 * Can be moved up and down as part of teleop or autonomous control to move the {@link Intake}
 * (attached to the end of the Wrist) closer to a game piece or game element (eg node in the
 * field, human player station), at which point the {@link Intake} can grab or release the game
 * piece as appropriate.
 */
public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {
    /**
     * @param angle the current angle of the wrist.
     */
    public record WristStatus(double rotations, double angle) implements Status {
        public boolean isNearTo(Position position) {
            return isNearTo(position.getAngle());
        }

        public boolean isNearTo(double angle) {
            return Math.abs(angle - this.angle()) < NEAR_THRESHOLD;
        }
    }

    /**
     * Pre-set positions for the wrist.
     */
    public enum Position {

        /** Wrist is in top position.  Starting position. */
        TOP(-180),
        /** Wrist is in the position for moving around the field. */
        RETRACTED(-175.0),
        /** Wrist is level with ground. */
        LEVEL(-65),
        HIGH_NODE(-20),
        MID_NODE(-25.5),
        HUMAN_CONES(-4),
        HUMAN_CUBES(-8),
        /** Wrist is fully down. **/
        BOTTOM(60);

        private final double angle;

        Position(double angle) {
            this.angle = angle;
        }

        private double getAngle() {
            return angle;
        }
    }

    private static final double NUDGE_INCREMENT = 5.0;
    private static final double NUDGE_DAMPENER = 0.15;

    private static final double NEAR_THRESHOLD = 5.0;

    private final SparkMax motor;
    private final SparkClosedLoopController pidController;
    private final ValueProvider<Double> pGain;
    private final ValueProvider<Double> iGain;
    private final ValueProvider<Double> dGain;
    private final ValueProvider<Double> ffGain;

    /**
     * Contructs a new Wrist.
     */
    public Wrist() {
        MotorController halMotor = RobotProvider.instance.getMotor(WRIST_MOTOR);
        if (!(halMotor instanceof SparkMax)) {
            log(Severity.ERROR, "Motor is not a CANSparkMax!");
            throw new IllegalStateException("Motor is not a CANSparkMax!");
        }
        motor = (SparkMax) halMotor;

        motor.getEncoder()
                .setPosition(EncoderUtils.wristDegreesToRotations(Position.TOP.getAngle()));

        // stash the PIDController for convenience.  will update the PID values to the latest from
        // the config
        // file each time we use the motor.
        pidController = motor.getClosedLoopController();

        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.feedbackSensor(FeedbackSensor.kPrimaryEncoder);
        motor.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        // grab config values for PID.
        pGain = ConfigFileReader.getInstance().getDouble(WRIST_PGAIN);
        iGain = ConfigFileReader.getInstance().getDouble(WRIST_IGAIN);
        dGain = ConfigFileReader.getInstance().getDouble(WRIST_DGAIN);
        ffGain = ConfigFileReader.getInstance().getDouble(WRIST_FFGAIN);
    }

    public void nudgeNoPID(double value) {
        checkContextReservation();
        double clampedValue = MathUtil.clamp(value, -1, 1);
        clampedValue *= NUDGE_DAMPENER; // make nudges less forceful. TODO: make this non-linear
        motor.set(clampedValue);
    }

    public void stopWrist() {
        checkContextReservation();
        motor.set(0);
    }

    public void nudgeUp() {
        checkContextReservation();
        System.err.println("Nudging up.");
        double angle = getStatus().angle();
        double targetAngle = Math.max(angle - NUDGE_INCREMENT, Position.TOP.getAngle());
        System.err.println("Target: " + targetAngle);

        rotate(targetAngle);
    }

    public void nudgeDown() {
        checkContextReservation();
        System.err.println("Nudging down.");
        double angle = getStatus().angle();
        double targetAngle = Math.min(angle + NUDGE_INCREMENT, Position.BOTTOM.getAngle());
        rotate(targetAngle);
    }

    /**
     * Rotates the wrist to a pre-set {@link Position}.
     */
    public void rotate(Position position) {
        checkContextReservation();
        rotate(position.getAngle());
    }

    /**
     * Starts rotating the wrist to the specified angle.
     * NOTE: this method returns immediately.  Check the current wrist position of the wrist
     * with {@link #getAngle()}.
     */
    public void rotate(double angle) {
        checkContextReservation();

        System.err.println("Setting target angle to " + angle);
        // set the PID controller values with whatever the latest is in the config
        SparkMaxConfig config = new SparkMaxConfig();
        config.closedLoop.pid(pGain.get(), iGain.get(), dGain.get());
        config.closedLoop.outputRange(-1, 1);
        motor.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        double ff = ffGain.get() * Math.cos(Math.toRadians(angle));
        SmartDashboard.putNumber("[WRIST] ff", ff);
        SmartDashboard.putNumber("[WRIST] reference", angle);

        // convert the desired target degrees to rotations
        double rotations = EncoderUtils.wristDegreesToRotations(angle);

        // set the reference point for the wrist
        pidController.setReference(
                rotations,
                com.revrobotics.spark.SparkBase.ControlType.kPosition,
                ClosedLoopSlot.kSlot0,
                ff);
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(
                motor.getEncoder().getPosition(),
                EncoderUtils.wristRotationsToDegrees(motor.getEncoder().getPosition()));
    }
}
