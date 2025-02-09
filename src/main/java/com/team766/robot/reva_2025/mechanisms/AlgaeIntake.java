package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;
import com.team766.robot.reva_2025.constants.EncoderUtils;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;
    private static final double MIN_ANGLE = -60;
    private static final double MAX_ANGLE = 90;
    private static final double NUDGE_AMOUNT = 5;
    private static final double THRESHOLD_CONSTANT = 0; // TODO: Update me after testing!

    private ValueProvider<Double> ffGain;

    // TODO: Intake and shooter motor should drive when we shoot. Shooter motor should be slgithly
    // slower than the intake motor
    // to add backspin on the ball.

    public record AlgaeIntakeStatus(State state, Level level, double angleDeg, double innerRPM, double outerRPM) implements Status {
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
        state = State.Stop;
        armMotor.setSensorPosition(EncoderUtils.algaeArmDegreesToRotations(level.getAngle()));

        intakeMotor.setCurrentLimit(115);
        shooterMotor.setCurrentLimit(80);
    }

    public enum State {
        In(0, 0.75),
        Out(0, -0.25),
        Stop(0, 0),
        ShooterOn(1.0, 0),
        Feed(1.0,1);
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
        GroundIntake(-30, 1),
        Shoot(-15, 1),
        L2L3AlgaeIntake(20, 1),
        L3L4AlgaeIntake(70, -1),
        Stow(-60, 0);

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
        SmartDashboard.putNumber("Algae Intake Set Point", setPosition);
        if (setPosition >= MIN_ANGLE && setPosition <= MAX_ANGLE) {
            double ff = ffGain.valueOr(0.0) * Math.cos(Math.toRadians(setPosition));
            armMotor.set(
                    MotorController.ControlMode.Position,
                    EncoderUtils.algaeArmDegreesToRotations(setPosition),
                    ff);
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

    public void nudgeNoPID(double value) {
        armMotor.set(value);
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

    public void feed() {
        state = State.Feed;
        shooterMotor.set(state.getInnerPower());
        intakeMotor.set(state.getOuterPower());
    }

    public void shooterOn() {
        state = State.ShooterOn;
        shooterMotor.set(state.getInnerPower());
        intakeMotor.set(state.getOuterPower());
    }

    public void shooterOff() {
        state = State.Stop;
        shooterMotor.set(state.getInnerPower());
        intakeMotor.set(state.getOuterPower() * level.getPower());
    }

    protected void run() {
        SmartDashboard.putNumber("Inner Current", MotorUtil.getCurrentUsage(shooterMotor));
        SmartDashboard.putNumber("Outer Current", MotorUtil.getCurrentUsage(intakeMotor));
        SmartDashboard.putNumber("Inner Get", shooterMotor.get());
        SmartDashboard.putNumber("Outer Get", intakeMotor.get());
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
                EncoderUtils.algaeArmRotationsToDegrees(armMotor.getSensorPosition()),
                shooterMotor.getSensorVelocity() * 60,
                intakeMotor.getSensorVelocity() * 60);
    }
}
