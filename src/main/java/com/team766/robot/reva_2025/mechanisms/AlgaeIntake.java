package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
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

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(State state, Level level, double armAngle) implements Status {}

    public AlgaeIntake() {
        intakeMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_INTAKEROLLERMOTOR);
        armMotor = RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_ARMROLLERMOTOR);
        shooterMotor =
                RobotProvider.instance.getMotor(ConfigConstants.ALGAEINTAKE_SHOOTERROLLERMOTOR);

        level = Level.Stow;
        ffGain = ConfigFileReader.getInstance().getDouble(ConfigConstants.ALGAEINTAKE_ARMFFGAIN);
    }

    public enum State {
        In(1, 0),
        Idle(0, 0),
        Out(-1, 0),
        Shoot(0, 1);

        private final double intakePower;
        private final double shooterPower;

        State(double intakePower, double shooterPower) {
            this.intakePower = intakePower;
            this.shooterPower = shooterPower;
        }

        private double getIntakePower() {
            return intakePower;
        }

        private double getShooterPower() {
            return shooterPower;
        }
    }

    public enum Level {
        GroundIntake(20, 1),
        L2L3AlgaeIntake(90, 1),
        L3L4AlgaeIntake(180, -1),
        Stow(0, 0);

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
        // armMotor.set(MotorController.ControlMode.Position, level.getAngle());
        setArmAngle(level.getAngle());
    }

    public void setArmAngle(double angle) {
        // armMotor.set(MotorController.ControlMode.Position, angle);
        this.targetAngle =
                com.team766.math.Math.clamp(
                        angle, Level.Stow.getAngle(), Level.L3L4AlgaeIntake.getAngle());
        armMotor.set(
                MotorController.ControlMode.Position, EncoderUtils.armDegreesToRotations(angle));
    }

    public void out() {
        state = State.Out;
    }

    public void stop() {
        state = State.Idle;
    }

    public void in() {
        state = State.In;
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    public void shooterOn() {
        state = State.Shoot;
        shooterMotor.set(state.getIntakePower());
    }

    public void shooterOff() {
        state = State.Idle;
        shooterMotor.set(state.getIntakePower());
    }

    @Override
    protected void run() {
        double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().armAngle()));
        armMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.armDegreesToRotations(targetAngle),
                ff);
        intakeMotor.set(level.getDirection() * state.getIntakePower());
        shooterMotor.set(state.getShooterPower());
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(
                state, level, EncoderUtils.armRotationsToDegrees(armMotor.getSensorPosition()));
    }

    public void nudgeArmUp() {
        double angle = getStatus().armAngle();
        angle = angle + 5;
        setArmAngle(angle);
    }

    public void nudgeArmDown() {
        double angle = getStatus().armAngle();
        angle = angle - 5;
        setArmAngle(angle);
    }
}
