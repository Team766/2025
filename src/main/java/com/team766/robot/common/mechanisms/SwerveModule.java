package com.team766.robot.common.mechanisms;

import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.reva.mechanisms.MotorUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Encapsulates the motors and encoders used for each physical swerve module and
 * provides driving and steering controls for each module.
 */
public class SwerveModule {
    private final String modulePlacement;
    private final MotorController drive;
    private final MotorController steer;
    private final EncoderReader encoder;
    private final double offset;

    // In meters
    private final double wheelCircumference;
    private final double driveGearRatio;
    private final int encoderToRevolutionConstant;

    /*
     * Factor that converts between motor rotations and wheel degrees
     * Multiply to convert from wheel degrees to motor rotations
     * Divide to convert from motor rotations to wheel degrees
     */
    public final double encoderConversionFactor;

    /*
     * Factor that converts between drive motor angular speed (rad/s) to drive wheel tip speed (m/s)
     * Multiply to convert from wheel tip speed to motor angular speed
     * Divide to convert from angular speed to wheel tip speed
     */
    public final double motorWheelFactorMPS;

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
            EncoderReader encoder,
            SwerveConfig config) {

        wheelCircumference = config.wheelCircumference();
        driveGearRatio = config.driveGearRatio();
        encoderToRevolutionConstant = config.encoderToRevolutionConstant();
        encoderConversionFactor =
                config.steerGearRatio() /*steering gear ratio*/
                        * (1. / 360.0) /*degrees to motor rotations*/;
        motorWheelFactorMPS =
                1.
                        / config.wheelRadius() // Wheel radians/sec
                        * driveGearRatio // Motor radians/sec
                        / (2 * Math.PI); // Motor rotations/sec (what velocity mode takes));

        this.modulePlacement = modulePlacement;
        this.drive = drive;
        this.steer = steer;
        this.encoder = encoder;
        this.offset = computeEncoderOffset();

        // Current limit for motors to avoid breaker problems
        drive.setCurrentLimit(config.driveMotorCurrentLimit());
        steer.setCurrentLimit(config.steerMotorCurrentLimit());
        // TODO: tune these values!
        MotorUtil.setTalonFXStatorCurrentLimit(drive, config.driveMotorStatorCurrentLimit());
        MotorUtil.setTalonFXStatorCurrentLimit(steer, config.steerMotorStatorCurrentLimit());
    }

    private double computeEncoderOffset() {
        return (steer.getSensorPosition() / encoderConversionFactor) % 360
                - (encoder.getPosition() * 360);
    }

    /**
     * Controls just the steer for this module.
     * Can be used to turn the wheels without moving
     * @param vector the vector specifying the module's motion
     */
    public void steer(Rotation2d angle) {

        // Add 360 * number of full rotations to angle, then add offset
        double realAngleDegrees =
                angle.getDegrees()
                        + 360
                                * (Math.round(
                                        (steer.getSensorPosition() / encoderConversionFactor
                                                        - offset
                                                        - angle.getDegrees())
                                                / 360))
                        + offset;

        // Sets the degree of the steer wheel
        // Needs to multiply by ENCODER_CONVERSION_FACTOR to translate into a unit the motor
        // understands
        steer.set(ControlMode.Position, encoderConversionFactor * realAngleDegrees);
    }

    /**
     * Controls both steer and power (based on the target vector) for this module.
     * @param vector the vector specifying the module's velocity in m/s and direction
     */
    public void driveAndSteer(Translation2d vector) {
        // checks if driving the wheel forward or backwards would be more efficient
        boolean reversed = Math.abs(vector.getAngle().minus(getSteerAngle()).getDegrees()) > 90;

        // apply the steer
        steer(reversed ? vector.getAngle().plus(Rotation2d.k180deg) : vector.getAngle());

        // sets the power to the magnitude of the vector and reverses power if necessary
        double power = vector.getNorm() * motorWheelFactorMPS * (reversed ? -1 : 1);
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
        return Rotation2d.fromDegrees(steer.getSensorPosition() / encoderConversionFactor - offset);
    }

    /**
     * Returns the encoder value of the drive motor in meters
     * @return drive motor encoder value, in meters
     */
    public double getDriveDisplacement() {
        return drive.getSensorPosition()
                * wheelCircumference
                / (driveGearRatio * encoderToRevolutionConstant);
    }

    public SwerveModuleState getModuleState() {
        return new SwerveModuleState(
                drive.getSensorVelocity() / motorWheelFactorMPS, getSteerAngle());
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
