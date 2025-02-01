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
        GroundIntake(20, -1),
        L2L3AlgaeIntake(90, -1),
        L3L4AlgaeIntake(180, 1),
        Stow(0, 0);

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
        intakeMotor.set(level.getPower());
        state = State.Out;
    }

    public void in() {
        intakeMotor.set(level.getPower() * -1);
        state = State.In;
    }

    public void stop() {
        intakeMotor.set(0);
        state = State.Stop;
    }

    public void shooterOn() {
        shooterMotor.set(1);
        state = State.Shoot;
    }

    public void shooterOff() {
        shooterMotor.set(0);
        state = State.Stop;
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(state, level);
    }
}
