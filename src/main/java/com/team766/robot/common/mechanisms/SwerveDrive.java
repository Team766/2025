package com.team766.robot.common.mechanisms;

import static com.team766.math.Math.normalizeAngleDegrees;
import static com.team766.robot.common.constants.ConfigConstants.*;

import com.ctre.phoenix6.hardware.CANcoder;
import com.team766.controllers.PIDController;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.framework3.StatusBus;
import com.team766.hal.GyroReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.localization.KalmanFilter;
import com.team766.localization.Odometry;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.orin.TimestampedApriltag;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.constants.ConfigConstants;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.reva_2025.mechanisms.Vision;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.checkerframework.checker.units.qual.C;

public class SwerveDrive extends MechanismWithStatus<SwerveDrive.DriveStatus> {
    /**
     * @param heading current heading in degrees
     */
    public static record DriveStatus(
            double heading,
            double pitch,
            double roll,
            Pose2d currentPosition,
            ChassisSpeeds robotOrientedChassisSpeeds,
            ChassisSpeeds fieldOrientedChassisSpeeds,
            SwerveModuleState[] swerveStates)
            implements Status {

        public boolean isAtRotationHeading(double targetHeading) {
            return Math.abs(normalizeAngleDegrees(targetHeading - heading))
                    < ControlConstants.AT_ROTATIONAL_ANGLE_THRESHOLD;
        }

        public boolean isAtRotationHeading(Rotation2d targetHeading) {
            return isAtRotationHeading(targetHeading.getDegrees());
        }

        public boolean isAtRotationVelocity(double omegaRadiansPerSecond) {
            return Math.abs(
                            omegaRadiansPerSecond
                                    - robotOrientedChassisSpeeds.omegaRadiansPerSecond)
                    < Math.toRadians(ControlConstants.AT_ROTATIONAL_SPEED_THRESHOLD);
        }

        public boolean isAtRobotOrientedSpeeds(
                ChassisSpeeds targetChassisSpeeds, boolean includeRotation) {
            return (!includeRotation
                            || isAtRotationVelocity(targetChassisSpeeds.omegaRadiansPerSecond))
                    && Math.abs(
                                    targetChassisSpeeds.vxMetersPerSecond
                                            - robotOrientedChassisSpeeds.vxMetersPerSecond)
                            < ControlConstants.AT_TRANSLATIONAL_SPEED_THRESHOLD
                    && Math.abs(
                                    targetChassisSpeeds.vyMetersPerSecond
                                            - robotOrientedChassisSpeeds.vyMetersPerSecond)
                            < ControlConstants.AT_TRANSLATIONAL_SPEED_THRESHOLD;
        }

        public boolean isAtFieldOrientedSpeeds(
                ChassisSpeeds targetChassisSpeeds, boolean includeRotation) {
            return isAtRobotOrientedSpeeds(
                    ChassisSpeeds.fromFieldRelativeSpeeds(
                            targetChassisSpeeds, Rotation2d.fromDegrees(heading)),
                    includeRotation);
        }
    }

    private final SwerveConfig config;

    // SwerveModules
    private final SwerveModule swerveFR;
    private final SwerveModule swerveFL;
    private final SwerveModule swerveBR;
    private final SwerveModule swerveBL;

    private final GyroReader gyro;

    // declaration of odometry object
    private Odometry swerveOdometry;
    // variable representing current position
    private KalmanFilter kalmanFilter;

    private Translation2d[] wheelPositions;
    private SwerveDriveKinematics swerveDriveKinematics;

    private PIDController rotationPID;

    private boolean movingToTarget = false;
    private double x;
    private double y;

    public SwerveDrive(SwerveConfig config) {
        this.config = config;

        // create the drive motors
        MotorController driveFR = RobotProvider.instance.getMotor(DRIVE_DRIVE_FRONT_RIGHT);
        MotorController driveFL = RobotProvider.instance.getMotor(DRIVE_DRIVE_FRONT_LEFT);
        MotorController driveBR = RobotProvider.instance.getMotor(DRIVE_DRIVE_BACK_RIGHT);
        MotorController driveBL = RobotProvider.instance.getMotor(DRIVE_DRIVE_BACK_LEFT);

        // create the steering motors
        MotorController steerFR = RobotProvider.instance.getMotor(DRIVE_STEER_FRONT_RIGHT);
        MotorController steerFL = RobotProvider.instance.getMotor(DRIVE_STEER_FRONT_LEFT);
        MotorController steerBR = RobotProvider.instance.getMotor(DRIVE_STEER_BACK_RIGHT);
        MotorController steerBL = RobotProvider.instance.getMotor(DRIVE_STEER_BACK_LEFT);

        // create the encoders
        CANcoder encoderFR = new CANcoder(2, config.canBus());
        CANcoder encoderFL = new CANcoder(4, config.canBus());
        CANcoder encoderBR = new CANcoder(3, config.canBus());
        CANcoder encoderBL = new CANcoder(1, config.canBus());

        // initialize the swerve modules
        swerveFR = new SwerveModule("FR", driveFR, steerFR, encoderFR, config);
        swerveFL = new SwerveModule("FL", driveFL, steerFL, encoderFL, config);
        swerveBR = new SwerveModule("BR", driveBR, steerBR, encoderBR, config);
        swerveBL = new SwerveModule("BL", driveBL, steerBL, encoderBL, config);

        // Sets up odometry
        gyro = RobotProvider.instance.getGyro(DRIVE_GYRO);

        rotationPID = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_ROTATION_PID);

        SwerveModule[] moduleList = new SwerveModule[] {swerveFR, swerveFL, swerveBR, swerveBL};
        double halfDistanceBetweenWheels = config.distanceBetweenWheels() / 2;
        this.wheelPositions =
                new Translation2d[] {
                    getPositionForWheel(config.frontRightLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.frontLeftLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.backRightLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.backLeftLocation(), halfDistanceBetweenWheels)
                };

        swerveDriveKinematics = new SwerveDriveKinematics(wheelPositions);

        swerveOdometry =
                new Odometry(
                        gyro,
                        moduleList,
                        config.wheelCircumference(),
                        config.driveGearRatio(),
                        config.encoderToRevolutionConstant());

        kalmanFilter = new KalmanFilter();
    }

    @Override
    public Category getLoggerCategory() {
        return Category.DRIVE;
    }

    /**
     * Helper method to create a new vector counterclockwise orthogonal to the given one
     * @param vector input vector
     * @return clockwise orthoginal output vector
     */
    private static Vector2D createOrthogonalVector(Vector2D vector) {
        return new Vector2D(-vector.getY(), vector.getX());
    }

    /**
     * Maps parameters to robot oriented swerve movement
     * @param x the x value for the position joystick, positive being forward
     * @param y the y value for the position joystick, positive being left
     * @param turn the turn value from the rotation joystick, positive being CCW
     */
    public void controlRobotOriented(double x, double y, double turn) {
        // Calculate the necessary turn velocity (m/s) for each motor:
        double turnVelocity = config.wheelDistanceFromCenter() * turn;

        // Finds the vectors for turning and for translation of each module, and adds them
        // Applies this for each module
        swerveFR.driveAndSteer(
                new Vector2D(x, y)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.frontRightLocation()).normalize()));
        swerveFL.driveAndSteer(
                new Vector2D(x, y)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.frontLeftLocation()).normalize()));
        swerveBR.driveAndSteer(
                new Vector2D(x, y)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.backRightLocation()).normalize()));
        swerveBL.driveAndSteer(
                new Vector2D(x, y)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.backLeftLocation()).normalize()));
    }

    public SwerveConfig getSwerveConfig() {
        return config;
    }

    /**
     * Uses controlRobotOriented() to control the robot relative to the field
     * @param x the x value for the position joystick, positive being forward, in meters/sec
     * @param y the y value for the position joystick, positive being left, in meters/sec
     * @param turn the turn value from the rotation joystick, positive being CCW, in radians/sec
     */
    private void controlFieldOrientedBase(double x, double y, double turn) {
        SmartDashboard.putString("Swerve Commands", "x: " + x + ", y: " + y + ", turn: " + turn);

        final Optional<Alliance> alliance = DriverStation.getAlliance();
        double yawRad =
                Math.toRadians(
                        getStatus().heading()
                                + (alliance.isPresent() && alliance.get() == Alliance.Blue
                                        ? 0
                                        : 180));

        // Applies a rotational translation to controlRobotOriented
        // Counteracts the forward direction changing when the robot turns
        controlRobotOriented(
                Math.cos(-yawRad) * x - Math.sin(-yawRad) * y,
                Math.sin(-yawRad) * x + Math.cos(-yawRad) * y,
                turn);
    }

    /**
     * Uses controlRobotOriented() to control the robot relative to the field
     * Sets robot to manual control mode rather than a rotation setpoint
     * @param x the x value for the position joystick, positive being forward, in meters/sec
     * @param y the y value for the position joystick, positive being left, in meters/sec
     * @param turn the turn value from the rotation joystick, positive being CCW, in radians/sec
     */
    public void controlFieldOriented(double x, double y, double turn) {
        movingToTarget = false;
        controlFieldOrientedBase(x, y, turn);
    }

    /**
     * Allows for field oriented control of the robot's position while moving to a specific angle for rotation
     * @param x the x value for the position joystick, positive being forward
     * @param y the y value for the position joystick, positive being left
     * @param target rotational target as a Rotation2d, can input a null value
     */
    public void controlFieldOrientedWithRotationTarget(double x, double y, Rotation2d target) {
        if (target != null) {
            rotationPID.setSetpoint(target.getDegrees());
            // SmartDashboard.putNumber("Rotation Target", target.getDegrees());
        }

        movingToTarget = true;
        this.x = x;
        this.y = y;
    }

    /**
     * Overloads controlFieldOriented to work with a chassisSpeeds input
     * @param chassisSpeeds
     */
    public void controlFieldOriented(ChassisSpeeds chassisSpeeds) {
        movingToTarget = false;

        double vx = chassisSpeeds.vxMetersPerSecond;
        double vy = chassisSpeeds.vyMetersPerSecond;
        double vang = chassisSpeeds.omegaRadiansPerSecond;

        controlFieldOrientedBase(vx, vy, vang);
    }

    /**
     * Overloads controlFieldOriented to work with a chassisSpeeds input
     * @param chassisSpeeds
     */
    public void controlRobotOriented(ChassisSpeeds chassisSpeeds) {
        movingToTarget = false;

        double vx = chassisSpeeds.vxMetersPerSecond;
        double vy = chassisSpeeds.vyMetersPerSecond;
        double vang = chassisSpeeds.omegaRadiansPerSecond;

        controlRobotOriented(vx, vy, vang);
    }

    /*
     * Stops each drive motor and turns wheels in a cross formation to prevent robot from moving
     */
    public void stopDrive() {
        swerveFR.stopDrive();
        swerveFL.stopDrive();
        swerveBR.stopDrive();
        swerveBL.stopDrive();
        swerveFR.steer(config.frontRightLocation());
        swerveFL.steer(config.frontLeftLocation());
        swerveBR.steer(config.backRightLocation());
        swerveBL.steer(config.backLeftLocation());
    }

    /**
     * Resets gyro to zero degrees relative to the driver
     * Sets to 180 degrees if the driver is on red (facing backwards)
     */
    public void resetGyro() {
        final Optional<Alliance> alliance = DriverStation.getAlliance();
        resetGyro(alliance.isPresent() && alliance.get().equals(Alliance.Blue) ? 0 : 180);
    }

    /**
     * Sets gyro to value in degrees
     * @param angle in degrees
     */
    public void resetGyro(double angle) {
        gyro.setAngle(angle);
    }

    public void setCurrentPosition(Pose2d P) {
        kalmanFilter.setPos(P.getTranslation());
    }

    public void resetCurrentPosition() {
        kalmanFilter.setPos(new Translation2d());
    }

    private static Translation2d getPositionForWheel(
            Vector2D relativeLocation, double halfDistance) {
        return new Translation2d(
                relativeLocation.getX() * halfDistance, relativeLocation.getY() * halfDistance);
    }

    @Override
    protected void onMechanismIdle() {
        stopDrive();
    }

    // Odometry
    @Override
    public DriveStatus updateStatus() {
        kalmanFilter.addOdometryInput(
                swerveOdometry.calculateCurrentPositionChange(),
                RobotProvider.instance.getClock().getTime());

        final double heading = gyro.getAngle();
        final double pitch = gyro.getPitch();
        final double roll = gyro.getRoll();

        var visionStatus = StatusBus.getInstance().getStatus(Vision.VisionStatus.class);
        if (visionStatus.isPresent() && !visionStatus.get().allTags().isEmpty()) {
            for (List<TimestampedApriltag> cameraTags : visionStatus.get().allTags()) {
                List<Translation2d> tagPoses = new ArrayList<>();
                if (cameraTags.size() > 0) {
                    for (TimestampedApriltag tag : cameraTags) {
                        tagPoses.add(tag.toRobotPosition(Rotation2d.fromDegrees(heading)));
                    }
                    kalmanFilter.updateWithVisionMeasurement(
                            tagPoses, cameraTags.get(0).getCollectTime());
                }
            }
        }

        final Pose2d currentPosition =
                new Pose2d(kalmanFilter.getPos(), Rotation2d.fromDegrees(heading));

        final ChassisSpeeds robotOrientedChassisSpeeds =
                swerveDriveKinematics.toChassisSpeeds(
                        swerveFR.getModuleState(),
                        swerveFL.getModuleState(),
                        swerveBR.getModuleState(),
                        swerveBL.getModuleState());

        final ChassisSpeeds fieldOrientedChassisSpeeds =
                ChassisSpeeds.fromRobotRelativeSpeeds(
                        robotOrientedChassisSpeeds, Rotation2d.fromDegrees(heading));

        swerveFR.dashboardCurrentUsage();
        swerveFL.dashboardCurrentUsage();
        swerveBR.dashboardCurrentUsage();
        swerveBL.dashboardCurrentUsage();

        SwerveModuleState[] swerveModuleStates =
                new SwerveModuleState[] {
                    swerveFR.getModuleState(),
                    swerveFL.getModuleState(),
                    swerveBR.getModuleState(),
                    swerveBL.getModuleState(),
                };
        if (Logger.isLoggingToDataLog()) {
            org.littletonrobotics.junction.Logger.recordOutput("curPose", currentPosition);
            org.littletonrobotics.junction.Logger.recordOutput(
                    "current rotational velocity",
                    robotOrientedChassisSpeeds.omegaRadiansPerSecond);
            org.littletonrobotics.junction.Logger.recordOutput("SwerveStates", swerveModuleStates);
        }

        return new DriveStatus(
                heading,
                pitch,
                roll,
                currentPosition,
                robotOrientedChassisSpeeds,
                fieldOrientedChassisSpeeds,
                swerveModuleStates);
    }

    @Override
    protected void run() {
        if (movingToTarget) {
            rotationPID.calculate(getStatus().heading());
            controlFieldOrientedBase(
                    x,
                    y,
                    (Math.abs(rotationPID.getOutput()) < ControlConstants.DEFAULT_ROTATION_THRESHOLD
                            ? 0
                            : rotationPID.getOutput()));
        }
    }
}
