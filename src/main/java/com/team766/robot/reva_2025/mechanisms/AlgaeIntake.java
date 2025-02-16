package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;
    private double targetAngle;
    private final ValueProvider<Double> ffGain;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private static final double NUDGE_AMOUNT = 1;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(
            State state, double direction, double targetAngle, double currentAngle)
            implements Status {
        public boolean isAtAngle() {
            return Math.abs(targetAngle() - currentAngle()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public AlgaeIntake() {
        intakeMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_INTAKEROLLERMOTOR);
        armMotor = RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_ARMROLLERMOTOR);
        shooterMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_SHOOTERROLLERMOTOR);

        state = State.Idle;
        level = Level.Stow;
        ffGain = ConfigFileReader.getInstance().getDouble(ConfigConstants.ALGAEINTAKE_ARMFFGAIN);
    }

    public enum State {
        // velocity is in revolutions per minute
        In(30000, 0),
        Idle(0, 0),
        Out(-30000, 0),
        Shoot(0, 30000),
        Feed(50000, 30000);

        private final double intakeVelocity;
        private final double shooterVelocity;

        State(double intakeVelocity, double shooterVelocity) {
            this.intakeVelocity = intakeVelocity;
            this.shooterVelocity = shooterVelocity;
        }

        private double getIntakeVelocity() {
            // converting revolutions per minute to revolutions per 100 milliseconds
            return intakeVelocity / 60 / 10;
        }

        private double getShooterVelocity() {
            // converting revolutions per minute to revolutions per 100 milliseconds
            return shooterVelocity / 60 / 10;
        }
    }

    public enum Level {
        GroundIntake(-40, 1),
        L2L3AlgaeIntake(20, -1),
        L3L4AlgaeIntake(70, -1),
        Stow(-80, 1),
        Shoot(-25, 1); // placeholder number

        private final double angle;
        private final double direction;

        Level(double angle, double direction) {
            this.angle = angle;
            this.direction = direction;
        }

        private double getAngle() {
            return angle;
        }

        private double getDirection() {
            return direction;
        }
    }

    public void setArmAngle(Level level) {
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
        setState(State.Idle);
    }

    @Override
    protected void run() {
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().currentAngle()));
        armMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.algaeArmDegreesToRotations(targetAngle),
                ff);
        intakeMotor.set(ControlMode.Velocity, level.getDirection() * state.getIntakeVelocity());
        shooterMotor.set(ControlMode.Velocity, state.getShooterVelocity());
    }

    @Override
    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(
                state,
                level.getDirection(),
                targetAngle,
                EncoderUtils.algaeArmDegreesToRotations(armMotor.getSensorPosition()));
    }

    public void nudge(double sign) {
        double nudgePosition = getStatus().currentAngle() + (NUDGE_AMOUNT * Math.signum(sign));
        setArmAngle(nudgePosition);
    }
}
