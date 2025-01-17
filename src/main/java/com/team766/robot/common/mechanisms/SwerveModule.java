package com.team766.robot.common.mechanisms;

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.CANcoder;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.reva.mechanisms.MotorUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Encapsulates the motors and encoders used for each physical swerve module and
 * provides driving and steering controls for each module.
 */
public class SwerveModule {
    private final String modulePlacement;
    private final MotorController drive;
    private final MotorController steer;
    private final CANcoder encoder;
    private final double offset;

    // In meters
    private final double WHEEL_CIRCUMFERENCE;
    private final double DRIVE_GEAR_RATIO;
    private final int ENCODER_TO_REVOLUTION_CONSTANT;

    /*
     * Factor that converts between motor rotations and wheel degrees
     * Multiply to convert from wheel degrees to motor rotations
     * Divide to convert from motor rotations to wheel degrees
     */
    public final double ENCODER_CONVERSION_FACTOR;

    /*
     * Factor that converts between drive motor angular speed (rad/s) to drive wheel tip speed (m/s)
     * Multiply to convert from wheel tip speed to motor angular speed
     * Divide to convert from angular speed to wheel tip speed
     */
    public final double MOTOR_WHEEL_FACTOR_MPS;

    /**
     * Creates a new SwerveModule.
     *
     * @param modulePlacement String description of the placement for this module, eg "FL".
     * @param drive Drive MotorController for this module.
     * @param steer Steer MotorController for this module.
     * @param encoder CANCoder for this module.
     */
    public SwerveModule(
            String modulePlacement,
            MotorController drive,
            MotorController steer,
            CANcoder encoder,
            SwerveConfig config) {

        WHEEL_CIRCUMFERENCE = config.wheelCircumference();
        DRIVE_GEAR_RATIO = config.driveGearRatio();
        ENCODER_TO_REVOLUTION_CONSTANT = config.encoderToRevolutionConstant();
        ENCODER_CONVERSION_FACTOR =
                config.steerGearRatio() /*steering gear ratio*/
                        * (1. / 360.0) /*degrees to motor rotations*/;
        MOTOR_WHEEL_FACTOR_MPS =
                1.
                        / config.wheelRadius() // Wheel radians/sec
                        * DRIVE_GEAR_RATIO // Motor radians/sec
                        / (2 * Math.PI); // Motor rotations/sec (what velocity mode takes));

        this.modulePlacement = modulePlacement;
        this.drive = drive;
        this.steer = steer;
        this.encoder = encoder;
        this.offset = computeEncoderOffset();
        // SmartDashboard.putNumber("[" + modulePlacement + "]" + "Offset", offset);

        // Current limit for motors to avoid breaker problems
        drive.setCurrentLimit(config.driveMotorCurrentLimit());
        steer.setCurrentLimit(config.steerMotorCurrentLimit());
        // TODO: tune these values!
        MotorUtil.setTalonFXStatorCurrentLimit(drive, config.driveMotorStatorCurrentLimit());
        MotorUtil.setTalonFXStatorCurrentLimit(steer, config.steerMotorStatorCurrentLimit());
    }

    private double computeEncoderOffset() {
        StatusSignal<Angle> value = encoder.getAbsolutePosition();
        if (!value.getStatus().isOK()) {
            Logger.get(Category.DRIVE)
                    .logData(
                            Severity.ERROR,
                            "%s unable to read encoder: %s",
                            modulePlacement,
                            value.getStatus().toString());
            return 0; // ??
        }
        return (steer.getSensorPosition() / ENCODER_CONVERSION_FACTOR) % 360
                - (value.getValueAsDouble() * 360);
    }

    /**
     * Controls just the steer for this module.
     * Can be used to turn the wheels without moving
     * @param vector the vector specifying the module's motion
     */
    public void steer(Vector2D vector) {
        boolean reversed = false;
        SmartDashboard.putString(
                "[" + modulePlacement + "]" + "x, y",
                String.format("%.2f, %.2f", vector.getX(), vector.getY()));

        // Calculates the angle of the vector from -180° to 180°
        final double vectorTheta = Math.toDegrees(Math.atan2(vector.getY(), vector.getX()));

        // Add 360 * number of full rotations to vectorTheta, then add offset
        double realAngleDegrees =
                vectorTheta
                        + 360
                                * (Math.round(
                                        (steer.getSensorPosition() / ENCODER_CONVERSION_FACTOR
                                                        - offset
                                                        - vectorTheta)
                                                / 360))
                        + offset;
        // double degreeChange =
        //         realAngleDegrees - (steer.getSensorPosition() / ENCODER_CONVERSION_FACTOR);
        // checks if it would be more efficient to move the wheel in the opposite direction
        // if (degreeChange > 90) {
        //     realAngleDegrees -= 180;
        //     reversed = true;
        // } else if (degreeChange < -90) {
        //     realAngleDegrees += 180;
        //     reversed = true;
        // } else {
        //     reversed = false;
        // }
        final double angleDegrees = realAngleDegrees;

        // Sets the degree of the steer wheel
        // Needs to multiply by ENCODER_CONVERSION_FACTOR to translate into a unit the motor
        // understands
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + "Steer", angleDegrees);

        steer.set(ControlMode.Position, ENCODER_CONVERSION_FACTOR * angleDegrees);

        SmartDashboard.putNumber("[" + modulePlacement + "]" + "TargetAngle", vectorTheta);
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + "RelativeAngle",
        //         (steer.getSensorPosition() / ENCODER_CONVERSION_FACTOR - offset) % 360);
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + "CANCoder",
        //         encoder.getAbsolutePosition().getValueAsDouble() * 360);
        // return reversed;
    }

    /**
     * Controls both steer and power (based on the target vector) for this module.
     * @param vector the vector specifying the module's velocity in m/s and direction
     */
    public void driveAndSteer(Vector2D vector) {
        // apply the steer
        steer(vector);

        // sets the power to the magnitude of the vector and reverses power if necessary
        // TODO: does this need to be clamped to a specific range, eg btn -1 and 1?
        // SmartDashboard.putNumber("[" + modulePlacement + "]" + "Desired drive",
        // vector.getNorm());
        double power;
        // if (reversed) {
        //    power = -vector.getNorm() * MOTOR_WHEEL_FACTOR_MPS;
        //    reversed = false;

        // } else {
        power = vector.getNorm() * MOTOR_WHEEL_FACTOR_MPS;
        // }
        SmartDashboard.putNumber("[" + modulePlacement + "]" + "Input motor velocity", power);
        drive.set(ControlMode.Velocity, power);

        SmartDashboard.putNumber(
                "[" + modulePlacement + "]" + "Read Vel", drive.getSensorVelocity());
    }

    /**
     * Stops the drive motor for this module.
     */
    public void stopDrive() {
        drive.stopMotor();
    }

    public Rotation2d getSteerAngle() {
        return Rotation2d.fromDegrees(
                steer.getSensorPosition() / ENCODER_CONVERSION_FACTOR - offset);
    }

    /**
     * Returns the encoder value of the drive motor in meters
     * @return drive motor encoder value, in meters
     */
    public double getDriveDisplacement() {
        return drive.getSensorPosition()
                * WHEEL_CIRCUMFERENCE
                / (DRIVE_GEAR_RATIO * ENCODER_TO_REVOLUTION_CONSTANT);
    }

    public SwerveModuleState getModuleState() {
        return new SwerveModuleState(
                drive.getSensorVelocity() / MOTOR_WHEEL_FACTOR_MPS, getSteerAngle());
    }

    public void dashboardCurrentUsage() {
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + " steer supply current",
        //         MotorUtil.getCurrentUsage(steer));
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + " steer stator current",
        //         MotorUtil.getStatorCurrentUsage(steer));
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + " drive supply current",
        //         MotorUtil.getCurrentUsage(drive));
        // SmartDashboard.putNumber(
        //         "[" + modulePlacement + "]" + " drive stator current",
        //         MotorUtil.getStatorCurrentUsage(drive));
    }
}
