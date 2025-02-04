package com.team766.robot.reva_2025.mechanisms;

import com.ctre.phoenix6.controls.PositionDutyCycle;
import com.ctre.phoenix6.hardware.TalonFX;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva_2025.constants.EncoderUtils;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;
    private static final double MIN_ANGLE = 0;
    private static final double MAX_ANGLE = 150;
    private static final double NUDGE_AMOUNT = 5;
    private static final double THRESHOLD_CONSTANT = 0; // TODO: Update me after testing!

    private ValueProvider<Double> ffGain;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(State state, Level level, double angleDeg) implements Status {
        public boolean isAtPosition(double target) {
            return (Math.abs(target - angleDeg()) < THRESHOLD_CONSTANT);
        }
    }

    public AlgaeIntake() {
        intakeMotor = RobotProvider.instance.getMotor("algaeIntake.IntakeMotor");
        armMotor = RobotProvider.instance.getMotor("algaeIntake.ArmMotor");
        ffGain = ConfigFileReader.getInstance().getDouble("algaeIntake.ArmMotor.ffGain");
        shooterMotor = RobotProvider.instance.getMotor("algaeIntake.ShooterMotor");

        level = Level.Stow;
        armMotor.setSensorPosition(level.getAngle());
    }

    public enum State {
        In(0, 0.25),
        Out(0, -0.25),
        Stop(0, 0),
        Shoot(0.75, 1);
        private final double innerPower, outerPower;

        State(double innerPower, double outerPower) {
            this.innerPower = innerPower;
            this.outerPower = outerPower;
        }

        private double getInnerPower() {
            return innerPower;
        }

        private double getOuterPower() {
            return outerPower;
        }
    }

    public enum Level {
        GroundIntake(20, -1),
        Shoot(60, -1),
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

    /**
     *
     * @param setPosition in degrees
     */
    public void setPosition(double setPosition) {
        if (setPosition >= MIN_ANGLE && setPosition <= MAX_ANGLE) {
            TalonFX talon = (TalonFX) armMotor;
            double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(getStatus().angleDeg()));
            PositionDutyCycle positionRequest = new PositionDutyCycle(setPosition);
            positionRequest.FeedForward = ff;
            talon.setControl(positionRequest);
        }
    }

    public void setArmAngle(Level level) {
        setPosition(level.getAngle());
        this.level = level;
    }

    public void nudgeUp() {
        double nudgePosition = getStatus().angleDeg() + NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void nudgeDown() {
        double nudgePosition = getStatus().angleDeg() - NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void out() {
        state = State.Out;
        intakeMotor.set(state.getOuterPower() * level.getPower());
    }

    public void in() {
        state = State.In;
        intakeMotor.set(state.getOuterPower() * level.getPower());
    }

    public void stop() {
        state = State.Stop;
        intakeMotor.set(state.getOuterPower());
    }

    public void shooterOn() {
        state = State.Shoot;
        shooterMotor.set(state.getInnerPower());
        intakeMotor.set(state.getOuterPower());
    }

    public void shooterOff() {
        state = State.Stop;
        shooterMotor.set(state.getInnerPower());
        intakeMotor.set(state.getOuterPower() * level.getPower());
    }

    @Override
    protected void onMechanismIdle() {
        stop();
        shooterOff();
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(
                state,
                level,
                EncoderUtils.algaeArmRotationsToDegrees(armMotor.getSensorPosition()));
    }
}
