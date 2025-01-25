package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.library.RateLimiter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {
    public record ShooterStatus(
            double targetSpeed, double shooterSpeedTop, double shooterSpeedBottom)
            implements Status {
        public boolean isCloseToTargetSpeed() {
            return isCloseToSpeed(targetSpeed());
        }

        public boolean isCloseToSpeed(double targetSpeed) {
            return ((Math.abs(targetSpeed - shooterSpeedTop()) < SPEED_TOLERANCE)
                    && (Math.abs(targetSpeed - shooterSpeedBottom()) < SPEED_TOLERANCE));
        }
    }

    public static final double DEFAULT_SPEED =
            4800.0; // motor shaft rps, does not take gearing into account
    public static final double SHOOTER_ASSIST_SPEED = 4000.0;
    private static final double NUDGE_INCREMENT = 100.0;
    private static final double CURRENT_LIMIT = 40.0; // needs tuning
    private static final double MAX_SPEED = 5600.0; // spec is 6000.0
    private static final double MIN_SPEED = 0.0;

    // TODO: Get the voltage of the battery and set the speed tolerance propotional to this
    private static final double SPEED_TOLERANCE = 200.0; // rpm

    private MotorController shooterMotorTop;
    private MotorController shooterMotorBottom;
    private RateLimiter setSpeedLimiter = new RateLimiter(0.1);
    private boolean shouldRun = false;
    // only used if shouldRun is true
    private double targetSpeed = DEFAULT_SPEED;
    private boolean speedUpdated = false;

    public Shooter() {
        shooterMotorTop = RobotProvider.instance.getMotor(SHOOTER_MOTOR_TOP);
        shooterMotorBottom = RobotProvider.instance.getMotor(SHOOTER_MOTOR_BOTTOM);
        SparkMax canTop = (SparkMax) shooterMotorTop;
        SparkMaxConfig topConfig = new SparkMaxConfig();
        topConfig.voltageCompensation(12.0);
        canTop.configure(
                topConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        SparkMax canBottom = (SparkMax) shooterMotorBottom;
        SparkMaxConfig bottomConfig = new SparkMaxConfig();
        bottomConfig.voltageCompensation(12.0);
        canBottom.configure(
                topConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters);

        shooterMotorTop.setNeutralMode(NeutralMode.Coast);
        shooterMotorBottom.setNeutralMode(NeutralMode.Coast);
        shooterMotorTop.setCurrentLimit(CURRENT_LIMIT);
        shooterMotorBottom.setCurrentLimit(CURRENT_LIMIT);
    }

    public void shoot(double speed) {
        targetSpeed = com.team766.math.Math.clamp(speed, MIN_SPEED, MAX_SPEED);
        shoot();
    }

    public void shoot() {
        shouldRun = targetSpeed > 0.0;
        speedUpdated = true;
    }

    public void stop() {
        shouldRun = false;
        speedUpdated = true;
    }

    public void nudgeUp() {
        shoot(Math.min(targetSpeed + NUDGE_INCREMENT, MAX_SPEED));
    }

    public void nudgeDown() {
        shoot(Math.max(targetSpeed - NUDGE_INCREMENT, MIN_SPEED));
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected void run() {
        // FIXME: problem with this - does not pay attention to changes in PID values
        // https://github.com/Team766/2024/pull/49 adds support to address this
        // until then, this is equivalent to the earlier approach
        if (speedUpdated || setSpeedLimiter.next()) {
            SmartDashboard.putNumber("[SHOOTER TARGET SPEED]", shouldRun ? targetSpeed : 0.0);

            if (shouldRun) {
                shooterMotorTop.set(ControlMode.Velocity, targetSpeed);
                shooterMotorBottom.set(ControlMode.Velocity, targetSpeed);
            } else {
                shooterMotorTop.stopMotor();
                shooterMotorBottom.stopMotor();
            }
            speedUpdated = false;
        }
    }

    @Override
    protected ShooterStatus updateStatus() {
        // SmartDashboard.putNumber(
        //         "[SHOOTER] Top Motor Current", MotorUtil.getCurrentUsage(shooterMotorTop));
        // SmartDashboard.putNumber(
        //         "[SHOOTER] Bottom Motor Current",
        //         MotorUtil.getCurrentUsage(shooterMotorBottom));
        SmartDashboard.putNumber("[SHOOTER TOP MOTOR SPEED]", shooterMotorTop.getSensorVelocity());
        SmartDashboard.putNumber(
                "[SHOOTER BOTTOM MOTOR SPEED]", shooterMotorBottom.getSensorVelocity());

        return new ShooterStatus(
                targetSpeed,
                shooterMotorTop.getSensorVelocity(),
                shooterMotorBottom.getSensorVelocity());
    }
}
