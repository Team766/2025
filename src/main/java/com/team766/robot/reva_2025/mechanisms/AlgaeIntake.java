package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;
    private final static double NUDGE_AMOUNT = 1.0;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(State state, Level level) implements Status {}

    public AlgaeIntake() {
        intakeMotor = RobotProvider.instance.getMotor("AlgaeIntake.RollerMotor");
        armMotor = RobotProvider.instance.getMotor("AlgaeArm.RollerMotor");
        shooterMotor = RobotProvider.instance.getMotor("AlgaeShooter.RollerMotor");

        level = Level.Stow;
    }

    public enum State {
        In(1),
        Idle(0),
        Out(-1),
        Stop(0),
        Shoot(1);

        private final double power;

        State(double power) {
            this.power = power;
        }

        private double getPower() {
            return power;
        }
    }

    public enum Level {
        Stow(0,0),
        GroundIntake(20, -1),
        L2_L3AlgaeIntake(90, -1),
        L3_L4AlgaeIntake(180, 1),
        // Shoot was Stow previously; set angle & power to the appropriate
        Shoot(0, 0);

        private final double angle;
        private final double power;

        Level(double angle, double power) {
            this.angle = angle;
            this.power = power;
        }

        private double getAngle() {
            return angle;
        }

        private double getPower() {
            return power;
        }
    }

    public void setArmAngle(Level level) {
        armMotor.set(MotorController.ControlMode.Position, level.getAngle());
        this.level = level;
    }

    public void out() {
        state = State.Out;
        intakeMotor.set(state.getPower());
    }

    public void in() {
        state = State.In;
        intakeMotor.set(state.getPower());
    }

    public void stop() {
        state = State.Stop;
        intakeMotor.set(state.getPower());
    }

    public void Idle() {
        state = State.Idle;
        intakeMotor.set(state.getPower());
    }

    public void shooterOn() {
        state = State.Shoot;
        shooterMotor.set(state.getPower());
    }

    public void shooterOff() {
        state = State.Stop;
        shooterMotor.set(state.getPower());
    }

    public void nudge(double multiplier) {
        double nudgePosition = armMotor.getSensorPosition() + (NUDGE_AMOUNT * multiplier);
        armMotor.set(MotorController.ControlMode.Position, nudgePosition);
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(state, level);
    }
}
