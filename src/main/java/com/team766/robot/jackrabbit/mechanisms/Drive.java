package com.team766.robot.jackrabbit.mechanisms;

import static com.team766.math.Maths.normalizeAngleDegrees;

import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.mechanisms.DifferentialMechanism;
import com.ctre.phoenix6.mechanisms.DifferentialMechanism.DifferentialPigeon2Source;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.robot.jackrabbit.HardwareConfig;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Optional;

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    private static final double AT_ROTATIONAL_ANGLE_THRESHOLD = 3.0; // TODO: Find actual value

    public static record DriveStatus(
            Optional<Pose2d> currentPosition, double yawRate, double pitch, double roll)
            implements Status {
        public boolean isAtRotationHeading(double targetHeading) {
            if (!currentPosition.isPresent()) {
                return false;
            }
            return Math.abs(
                            normalizeAngleDegrees(
                                    targetHeading
                                            - currentPosition.get().getRotation().getDegrees()))
                    < AT_ROTATIONAL_ANGLE_THRESHOLD;
        }

        public boolean isAtRotationHeading(Rotation2d targetHeading) {
            return isAtRotationHeading(targetHeading.getDegrees());
        }
    }

    private interface Mode extends Runnable {}

    private final TalonFX leftMotor;
    private final TalonFX rightMotor;
    private final Pigeon2 gyro;
    private final DifferentialMechanism differentialMechanism;

    private Mode mode;

    public Drive() {
        leftMotor =
                new TalonFX(
                        HardwareConfig.Motor.DRIVE_LEFT.canId(),
                        HardwareConfig.Motor.DRIVE_LEFT.canBus());
        rightMotor =
                new TalonFX(
                        HardwareConfig.Motor.DRIVE_RIGHT.canId(),
                        HardwareConfig.Motor.DRIVE_RIGHT.canBus());
        gyro =
                new Pigeon2(
                        HardwareConfig.Pigeon.DRIVE.canId(), HardwareConfig.Pigeon.DRIVE.canBus());
        differentialMechanism =
                new DifferentialMechanism(
                        rightMotor, leftMotor, false, gyro, DifferentialPigeon2Source.Yaw);

        // TODO: Set ratios for differential control

        stop();
    }

    public void stop() {
        leftMotor.stopMotor();
        rightMotor.stopMotor();

        mode = new Mode() {
            public void run() {
            }
        };
    }

    public void driveRobotOriented(double forward, double turn) {
        leftMotor.setVoltage(forward - turn);
        rightMotor.setVoltage(forward + turn);

        mode = new Mode() {
            public void run() {
            }
        };
    }

    public void driveAllianceOriented(double x, double y) {
        mode = new Mode() {
            public void run() {
                double targetPower = Math.hypot(x, y);
                double targetHeading = Math.atan2(y, x);
                if (DriverStation.getAlliance().orElse(null) != Alliance.Blue) {
                    targetHeading += 180;
                }
                if (!getStatus().isAtRotationHeading(targetHeading)) {
                    targetPower = 0.0;
                }
                differentialMechanism.setControl(
                        new VoltageOut(12 * targetPower), new PositionVoltage(targetHeading));
            }
        };
    }

    public void driveFieldOriented(double x, double y) {
        mode = new Mode() {
            public void run() {
                double targetPower = Math.hypot(x, y);
                double targetHeading = Math.atan2(y, x);
                if (!getStatus().isAtRotationHeading(targetHeading)) {
                    targetPower = 0.0;
                }
                differentialMechanism.setControl(
                        new VoltageOut(12 * targetPower), new PositionVoltage(targetHeading));
            }
        };
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected DriveStatus updateStatus() {
        // gyro.getYaw(false).getValueAsDouble(),
        Pose2d currentPosition = null; // TODO: localization

        return new DriveStatus(
                Optional.ofNullable(currentPosition),
                gyro.getAngularVelocityZWorld(false).getValueAsDouble(),
                gyro.getPitch(false).getValueAsDouble(),
                gyro.getRoll(false).getValueAsDouble());
    }

    @Override
    protected void run() {
        mode.run();
    }
}
