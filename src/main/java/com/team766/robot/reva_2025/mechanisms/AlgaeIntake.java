package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva_2025.constants.InputConstants;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;
    private double targetAngle;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(State state, Level level) implements Status {}

    public AlgaeIntake() {
        intakeMotor = RobotProvider.instance.getMotor(InputConstants.ALGAEINTAKE_INTAKEROLLERMOTOR);
        armMotor = RobotProvider.instance.getMotor(InputConstants.ALGAEINTAKE_ARMROLLERMOTOR);
        shooterMotor =
                RobotProvider.instance.getMotor(InputConstants.ALGAEINTAKE_SHOOTERROLLERMOTOR);

        level = Level.Stow;
    }

    public enum State {
        In(1),
        Idle(0),
        Out(-1),
        Shoot(1);

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
        this.targetAngle = angle;
    }

    public void out() {
        state = State.Out;
    }

    public void in() {
        state = State.In;
    }

    public void Idle() {
        state = State.Idle;
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
        // double ff = ffGain.value0r(default_value:0.0) *
        // Math.cos(Math.toRadians(getStatus().angle()));
        intakeMotor.set(
                MotorController.ControlMode.Position,
                EncoderUtils.wristDegreesToRotations(targetAngle),
                ff);
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(state, level);
    }
}
