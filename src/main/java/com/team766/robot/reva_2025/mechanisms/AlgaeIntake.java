package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.controllers.PIDController;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.hal.TimeOfFlightReader;
import com.team766.hal.TimeOfFlightReader.Range;
import com.team766.library.ValueProvider;
import com.team766.math.Maths;
import com.team766.robot.reva.mechanisms.MotorUtil;
import com.team766.robot.reva_2025.constants.ConfigConstants;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Optional;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private static final double ALGAE_HOLD_DISTANCE = 0.15;
    private static final double ALGAE_DETECTION_AMBIANCE = 90;
    private static final double STABLE_POSITION_THRESHOLD = 0.05;
    private static final double INTAKE_IDLE_RPS = 0;
    private static final double INTAKE_IN_MAX_RPS = 3 * 500. / 60.;
    private static final double SHOOTER_IDLE_RPS = 0;
    private static final double SHOOTER_IN_MAX_RPS = 500. / 60.;

    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private TimeOfFlightReader intakeSensor;
    private State state;
    private Level level;
    private double targetAngle;
    private boolean noPIDMode;
    private final EncoderReader absoluteEncoder;
    private final ValueProvider<Double> ffGain;
    private static final double POSITION_LOCATION_THRESHOLD = 0.1;
    private final PIDController holdAlgaeController;
    private static final double SHOOTER_SPEED_TOLERANCE = 100;
    private static final double NUDGE_AMOUNT = 5;
    private boolean encoderInitialized = false;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public static record AlgaeIntakeStatus(
            State state,
            Level level,
            double direction,
            double targetAngle,
            double currentAngle,
            Optional<Double> intakeProximity,
            double currentShooterSpeed)
            implements Status {
        public boolean isAtAngle() {
            return Math.abs(targetAngle() - currentAngle()) < POSITION_LOCATION_THRESHOLD;
        }

        public boolean isAtTargetSpeed() {
            return Math.abs(state().getShooterVelocity() - currentShooterSpeed)
                    < SHOOTER_SPEED_TOLERANCE;
        }

        public boolean isAlgaeStable() {
            if (intakeProximity.isEmpty()) return false;

            return java.lang.Math.abs(intakeProximity().get() - level.stablePosition())
                    < STABLE_POSITION_THRESHOLD;
        }
    }

    public AlgaeIntake() {
        intakeMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_INTAKEROLLERMOTOR);
        armMotor = RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_ARMROLLERMOTOR);
        shooterMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_SHOOTERROLLERMOTOR);
        intakeSensor =
                RobotProvider.instance.getTimeOfFlight(ConfigConstants.ALGAEINTAKE_INTAKESENSOR);
        intakeSensor.setRange(Range.Short);
        absoluteEncoder = RobotProvider.instance.getEncoder(ConfigConstants.ALGAE_INTAKE_ENCODER);

        intakeMotor.setCurrentLimit(50);
        shooterMotor.setCurrentLimit(80);

        state = State.Stop;
        level = Level.Stow;
        noPIDMode = false;
        targetAngle = level.getAngle();
        armMotor.setSensorPosition(EncoderUtils.algaeArmDegreesToRotations(targetAngle));
        ffGain = ConfigFileReader.getInstance().getDouble(ConfigConstants.ALGAEINTAKE_ARMFFGAIN);
        holdAlgaeController =
                PIDController.loadFromConfig(ConfigConstants.ALGAE_INTAKE_HOLD_ALGAE_PID);
    }

    public enum State {
        // velocity is in revolutions per minute
        In(3000. / 60., 0),
        InUntilStable(3000. / 60., 0), // velocities will be set automatically
        MatchVelocity(0, 0), // velocities will be set automatically
        Stop(0, 0),
        Out(-3000. / 60., 0),
        Shoot(0, 3500. / 60.),
        Feed(5500. / 60., 3500. / 60.),
        HoldAlgae(5500. / 60., 0), // velocities will be set automatically
        Idle(500. / 60., 500. / 60.);

        private final double intakeVelocity;
        private final double shooterVelocity;

        State(double intakeVelocity, double shooterVelocity) {
            this.intakeVelocity = intakeVelocity;
            this.shooterVelocity = shooterVelocity;
        }

        private double getIntakeVelocity() {
            return intakeVelocity;
        }

        private double getShooterVelocity() {
            return shooterVelocity;
        }
    }

    public enum Level {
        GroundIntake(-40, 1, 0.15, 0.31),
        L2L3AlgaeIntake(20, -1, 0.455, 0.70),
        L3L4AlgaeIntake(60, -1, 0.59, 0.62),
        Stow(-80, 1, 0.6, 0.28),
        Shoot(-25, 1, 0.15, 0.37); // placeholder number

        private final double angle;
        private final double direction;
        private final double stablePosition;
        private final double topPosition;

        private Level(double angle, double direction, double stablePosition, double topPosition) {
            this.angle = angle;
            this.direction = direction;
            this.stablePosition = stablePosition;
            this.topPosition = topPosition;
        }

        public double getAngle() {
            return angle;
        }

        public double getDirection() {
            return direction;
        }

        public double stablePosition() {
            return stablePosition;
        }

        public double topPosition() {
            return topPosition;
        }
    }

    public void setArmAngle(Level level) {
        this.level = level;
        setArmAngle(level.getAngle());
    }

    public void setArmAngle(double angle) {
        noPIDMode = false;
        this.targetAngle =
                Maths.clamp(angle, Level.Stow.getAngle(), Level.L3L4AlgaeIntake.getAngle());
    }

    public void nudge(double sign) {
        double nudgePosition = getStatus().currentAngle() + (NUDGE_AMOUNT * Math.signum(sign));
        setArmAngle(nudgePosition);
    }

    public void nudgeNoPID(double power) {
        noPIDMode = true;
        armMotor.set(power);
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    protected void onMechanismIdle() {
        setState(State.Stop);
    }

    private void setRpsForPosition(Optional<Double> position) {
        double top = level.topPosition();
        double bottom = level.stablePosition();
        if (position.isEmpty() || position.get() > top) {
            intakeMotor.set(ControlMode.Velocity, level.getDirection() * INTAKE_IN_MAX_RPS);
            shooterMotor.set(ControlMode.Velocity, -SHOOTER_IN_MAX_RPS);
            return;
        }
        if (position.get() < bottom) {
            intakeMotor.set(ControlMode.Velocity, level.getDirection() * INTAKE_IDLE_RPS);
            shooterMotor.set(ControlMode.Velocity, -SHOOTER_IDLE_RPS);
            return;
        }

        double intakeSlope = ((INTAKE_IN_MAX_RPS - INTAKE_IDLE_RPS) / (top - bottom));
        double intakeRps = (top - position.get()) * intakeSlope + INTAKE_IDLE_RPS;

        double shooterSlope = ((SHOOTER_IN_MAX_RPS - SHOOTER_IDLE_RPS) / (top - bottom));
        double shooterRps = (top - position.get()) * shooterSlope + SHOOTER_IDLE_RPS;

        intakeMotor.set(ControlMode.Velocity, level.getDirection() * intakeRps);
        shooterMotor.set(ControlMode.Velocity, -shooterRps);
    }

    private void matchArmRps() {
        // get the arm rps
        double armRps =
                EncoderUtils.algaeArmRotationsToDegrees(armMotor.getSensorVelocity()) / 360.;
        // set shooter rps to match (ccw+)
        shooterMotor.set(ControlMode.Velocity, armRps); // 1:1 gear ratio
        // set the intake rps to match, spinning the same direction (cw+)
        intakeMotor.set(ControlMode.Velocity, armRps * -3); // 3:1 gear ratio
    }

    @Override
    protected void run() {
        if (!noPIDMode) {

            double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().currentAngle()));
            armMotor.set(
                    MotorController.ControlMode.Position,
                    EncoderUtils.algaeArmDegreesToRotations(targetAngle),
                    ff);
        }
        switch (state) {
            case InUntilStable:
                if (getStatus().intakeProximity().isEmpty()) {
                    intakeMotor.set(ControlMode.Velocity, level.getDirection() * INTAKE_IN_MAX_RPS);
                    shooterMotor.set(ControlMode.Velocity, -SHOOTER_IN_MAX_RPS);
                } else {
                    holdAlgaeController.setSetpoint(level.stablePosition());
                    holdAlgaeController.calculate(getStatus().intakeProximity().get());
                    var output = holdAlgaeController.getOutput();
                    SmartDashboard.putNumber("Hold algae velocity", output);
                    intakeMotor.set(output);
                    shooterMotor.set(output / 3);
                }
                break;
            case MatchVelocity:
                matchArmRps();
                break;
            case HoldAlgae:
                if (getStatus().intakeProximity().isEmpty()) {
                    intakeMotor.set(
                            ControlMode.Velocity,
                            level.getDirection() * State.In.getIntakeVelocity());
                } else {
                    holdAlgaeController.setSetpoint(level.stablePosition());
                    holdAlgaeController.calculate(ALGAE_HOLD_DISTANCE);
                    var output = holdAlgaeController.getOutput();
                    SmartDashboard.putNumber("Hold algae velocity", output);
                    intakeMotor.set(output);
                }
                shooterMotor.stopMotor();
                break;
            case Shoot: // TODO: make not suck
                if (getStatus().intakeProximity().isEmpty()) {
                    intakeMotor.stopMotor();
                } else {
                    holdAlgaeController.setSetpoint(ALGAE_HOLD_DISTANCE);
                    holdAlgaeController.calculate(getStatus().intakeProximity().get());
                    var output = holdAlgaeController.getOutput();
                    SmartDashboard.putNumber("Hold algae velocity", output);
                    intakeMotor.set(output);
                }
                shooterMotor.set(ControlMode.Velocity, state.getShooterVelocity());
                break;
            case Stop:
                intakeMotor.stopMotor();
                shooterMotor.stopMotor();
                break;
            default:
                intakeMotor.set(
                        ControlMode.Velocity, level.getDirection() * state.getIntakeVelocity());
                shooterMotor.set(ControlMode.Velocity, state.getShooterVelocity());
                break;
        }
        SmartDashboard.putNumber("targetRPS", state.getIntakeVelocity());
        SmartDashboard.putNumber("actual RPS", intakeMotor.getSensorVelocity());
        SmartDashboard.putNumber(
                "Algae Shooter Current Limit", MotorUtil.getCurrentUsage(shooterMotor));
        SmartDashboard.putNumber(
                "Algae Intake Current Limit", MotorUtil.getCurrentUsage(intakeMotor));
    }

    @Override
    protected AlgaeIntakeStatus updateStatus() {
        if (!encoderInitialized && absoluteEncoder.isConnected()) {
            double angle =
                    (absoluteEncoder.getPosition() / 3.) * 360.
                            - 35.0; // offset so loops in the right spot
            SmartDashboard.putNumber("Algae Encoder Angle", angle);
            armMotor.setSensorPosition(EncoderUtils.algaeArmDegreesToRotations(angle));
            encoderInitialized = true;
        }

        Optional<Double> intakeProximity = intakeSensor.getDistance();
        Optional<Double> ambientSignal = intakeSensor.getAmbientSignal();

        SmartDashboard.putBoolean(
                "Algae last measurement valid", intakeSensor.wasLastMeasurementValid());

        return new AlgaeIntakeStatus(
                state,
                level,
                level.getDirection(),
                targetAngle,
                EncoderUtils.algaeArmRotationsToDegrees(armMotor.getSensorPosition()),
                ambientSignal.isPresent()
                                && intakeProximity.isPresent()
                                && ambientSignal.get() <= ALGAE_DETECTION_AMBIANCE
                        ? intakeProximity
                        : Optional.empty(),
                shooterMotor.getSensorVelocity());
    }
}
