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

public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
    private static final double AT_ROTATIONAL_ANGLE_THRESHOLD = 3.0; // TODO: Find actual value

    public static record DriveStatus(
            double heading, double yawRate, double pitch, double roll, Pose2d currentPosition)
            implements Status {
        public boolean isAtRotationHeading(double targetHeading) {
            return Math.abs(normalizeAngleDegrees(targetHeading - heading))
                    < AT_ROTATIONAL_ANGLE_THRESHOLD;
        }

        public boolean isAtRotationHeading(Rotation2d targetHeading) {
            return isAtRotationHeading(targetHeading.getDegrees());
        }
    }

    private sealed interface Mode {}

    private record Stop() implements Mode {}

    private record RobotOriented(double forward, double turn) implements Mode {}

    private record AllianceOriented(double x, double y) implements Mode {}

    private record FieldOriented(double x, double y) implements Mode {}

    private final TalonFX leftMotor;
    private final TalonFX rightMotor;
    private final Pigeon2 gyro;
    private final DifferentialMechanism differentialMechanism;

    private Mode mode = new Stop();

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
    }

    public void stop() {
        mode = new Stop();
    }

    public void driveRobotOriented(double forward, double turn) {
        mode = new RobotOriented(forward, turn);
    }

    public void driveAllianceOriented(double x, double y) {
        mode = new AllianceOriented(x, y);
    }

    public void driveFieldOriented(double x, double y) {
        mode = new FieldOriented(x, y);
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected DriveStatus updateStatus() {
        Pose2d currentPosition = null; // TODO: localization

        return new DriveStatus(
                gyro.getYaw(false).getValueAsDouble(),
                gyro.getAngularVelocityZWorld(false).getValueAsDouble(),
                gyro.getPitch(false).getValueAsDouble(),
                gyro.getRoll(false).getValueAsDouble(),
                currentPosition);
    }

    @Override
    protected void run() {
        switch (mode) {
            case Stop m -> {
                leftMotor.stopMotor();
                rightMotor.stopMotor();
            }
            case RobotOriented m -> {
                leftMotor.setVoltage(m.forward() - m.turn());
                rightMotor.setVoltage(m.forward() + m.turn());
            }
            case AllianceOriented m -> {
                double targetPower = Math.hypot(m.x(), m.y());
                double targetHeading = Math.atan2(m.y(), m.x());
                if (DriverStation.getAlliance().orElse(null) != Alliance.Blue) {
                    targetHeading += 180;
                }
                if (!getStatus().isAtRotationHeading(targetHeading)) {
                    targetPower = 0.0;
                }
                differentialMechanism.setControl(
                        new VoltageOut(12 * targetPower), new PositionVoltage(targetHeading));
            }
            case FieldOriented m -> {
                double targetPower = Math.hypot(m.x(), m.y());
                double targetHeading = Math.atan2(m.y(), m.x());
                if (!getStatus().isAtRotationHeading(targetHeading)) {
                    targetPower = 0.0;
                }
                differentialMechanism.setControl(
                        new VoltageOut(12 * targetPower), new PositionVoltage(targetHeading));
            }
        }
    }
}
