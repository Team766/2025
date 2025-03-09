package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.controllers.PIDController;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.hal.TimeOfFlightReader;
import com.team766.hal.TimeOfFlightReader.Range;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Optional;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private static final double ALGAE_HOLD_DISTANCE = 0.15;
    private static final double STABLE_POSITION_THRESHOLD = 0.05;
    private static final double INTAKE_IDLE_RPM = 500;
    private static final double INTAKE_IN_MAX_RPM = 3000;
    private static final double SHOOTER_IDLE_RPM = 0;
    private static final double SHOOTER_IN_MAX_RPM = 3000;

    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private TimeOfFlightReader intakeSensor;
    private State state;
    private Level level;
    private double targetAngle;
    private final ValueProvider<Double> ffGain;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private final PIDController holdAlgaeController;
    private static final double SHOOTER_SPEED_TOLERANCE = 100;
    private static final double NUDGE_AMOUNT = 1;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public static record AlgaeIntakeStatus(
            State state,
            Level level,
            double direction,
            double targetAngle,
            double currentAngle,
            double intakeProximity,
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
            return java.lang.Math.abs(intakeProximity() - level.stablePosition())
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

        intakeMotor.setCurrentLimit(115);
        shooterMotor.setCurrentLimit(80);

        state = State.Stop;
        level = Level.Stow;
        targetAngle = level.getAngle();
        armMotor.setSensorPosition(EncoderUtils.algaeArmDegreesToRotations(targetAngle));
        ffGain = ConfigFileReader.getInstance().getDouble(ConfigConstants.ALGAEINTAKE_ARMFFGAIN);
        holdAlgaeController =
                PIDController.loadFromConfig(ConfigConstants.ALGAE_INTAKE_HOLD_ALGAE_PID);
    }

    public enum State {
        // velocity is in revolutions per minute
        In(3000, 0),
        InUntilStable(3000, 0), // velocities will be set automatically
        MatchVelocity(0, 0), // velocities will be set automatically
        Stop(0, 0),
        Out(-3000, 0),
        Shoot(0, 3000),
        Feed(5000, 3000),
        HoldAlgae(5000, 0), // velocities will be set automatically
        Idle(500, 500);

        private final double intakeVelocity;
        private final double shooterVelocity;

        State(double intakeVelocity, double shooterVelocity) {
            this.intakeVelocity = intakeVelocity;
            this.shooterVelocity = shooterVelocity;
        }

        private double getIntakeVelocity() {
            return intakeVelocity / 60;
        }

        private double getShooterVelocity() {
            return shooterVelocity / 60;
        }
    }

    public enum Level {
        GroundIntake(-30, 1, 0.09, 0.31),
        L2L3AlgaeIntake(30, -1, 0.15, 0.37),
        L3L4AlgaeIntake(70, -1, 0.45, 0.67),
        Stow(-70, 1, 0.6, 0.28),
        Shoot(-10, 1, 0.15, 0.37);

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
        this.targetAngle =
                com.team766.math.Math.clamp(
                        angle, Level.Stow.getAngle(), Level.L3L4AlgaeIntake.getAngle());
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    protected void onMechanismIdle() {
        setState(State.Stop);
    }

    private void setRpmForPosition(double position) {
        double top = level.topPosition();
        double bottom = level.stablePosition();
        if (position < bottom) {
            intakeMotor.set(ControlMode.Velocity, level.getDirection() * INTAKE_IDLE_RPM);
            shooterMotor.set(ControlMode.Velocity, SHOOTER_IDLE_RPM);
        }
        if (position > top) {
            intakeMotor.set(ControlMode.Velocity, level.getDirection() * INTAKE_IN_MAX_RPM);
            shooterMotor.set(ControlMode.Velocity, SHOOTER_IN_MAX_RPM);
        }

        double intakeSlope = ((INTAKE_IN_MAX_RPM - INTAKE_IDLE_RPM) / (top - bottom));
        double intakeRpm = (top - position) * intakeSlope + INTAKE_IDLE_RPM;

        double shooterSlope = ((SHOOTER_IN_MAX_RPM - SHOOTER_IDLE_RPM) / (top - bottom));
        double shooterRpm = (top - position) * shooterSlope + SHOOTER_IDLE_RPM;

        intakeMotor.set(ControlMode.Velocity, level.getDirection() * intakeRpm);
        shooterMotor.set(ControlMode.Velocity, shooterRpm);
    }

    private void matchArmRpm() {
        // get the arm rpm
        double armRpm =
                EncoderUtils.algaeArmRotationsToDegrees(armMotor.getSensorVelocity()) / 360.;
        // set shooter rpm to match
        shooterMotor.set(ControlMode.Velocity, armRpm); // 1:1 gear ratio
        // set the intake rpm to match
        intakeMotor.set(ControlMode.Velocity, armRpm * 3); // 3:1 gear ratio
    }

    @Override
    protected void run() {
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().currentAngle()));
        armMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.algaeArmDegreesToRotations(targetAngle),
                ff);
        switch (state) {
            case InUntilStable:
                setRpmForPosition(getStatus().intakeProximity());
                break;
            case MatchVelocity:
                matchArmRpm();
                break;
            case HoldAlgae:
                holdAlgaeController.setSetpoint(ALGAE_HOLD_DISTANCE);
                holdAlgaeController.calculate(getStatus().intakeProximity());
                var output = holdAlgaeController.getOutput();
                SmartDashboard.putNumber("Hold algae velocity", output);
                intakeMotor.set(output);
                break;
            case Stop:
                intakeMotor.stopMotor();
                shooterMotor.stopMotor();
                break;
            default:
                intakeMotor.set(
                        ControlMode.Velocity, level.getDirection() * state.getIntakeVelocity());
                break;
        }
        shooterMotor.set(ControlMode.Velocity, state.getShooterVelocity());
        SmartDashboard.putNumber("targetRPM", state.getIntakeVelocity());
        SmartDashboard.putNumber("actual RPM", intakeMotor.getSensorVelocity());
    }

    @Override
    protected AlgaeIntakeStatus updateStatus() {
        Optional<Double> intakeProximity = intakeSensor.getDistance();
        Optional<Double> ambientSignal = intakeSensor.getAmbientSignal();

        return new AlgaeIntakeStatus(
                state,
                level,
                level.getDirection(),
                targetAngle,
                EncoderUtils.algaeArmDegreesToRotations(armMotor.getSensorPosition()),
                ambientSignal.isPresent()
                                && intakeProximity.isPresent()
                                && ambientSignal.get() <= 50
                        ? intakeProximity.get()
                        : 0,
                shooterMotor.getSensorVelocity() * 60 // rps to rpm
                );
    }

    public void nudge(double sign) {
        double nudgePosition = getStatus().currentAngle() + (NUDGE_AMOUNT * Math.signum(sign));
        setArmAngle(nudgePosition);
    }
}
