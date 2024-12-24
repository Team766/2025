package com.team766.robot.common.mechanisms;

import static com.team766.math.Math.normalizeAngleDegrees;
import static com.team766.robot.common.constants.ConfigConstants.*;

import com.ctre.phoenix6.hardware.CANcoder;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.util.PIDConstants;
import com.team766.config.ConfigFileReader;
import com.team766.controllers.PIDController;
import com.team766.framework3.Mechanism;
import com.team766.framework3.MultiFacetedMechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.GyroReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.odometry.Odometry;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.constants.ConfigConstants;
import com.team766.robot.common.constants.ControlConstants;
import com.team766.robot.common.constants.PathPlannerConstants;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class SwerveDrive extends MultiFacetedMechanism<SwerveDrive.DriveStatus> {
    /**
     * @param heading current heading in degrees
     */
    public static record DriveStatus(
            boolean isCrossed,
            double heading,
            double pitch,
            double roll,
            Pose2d currentPosition,
            ChassisSpeeds chassisSpeeds,
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
            return Math.abs(omegaRadiansPerSecond - chassisSpeeds.omegaRadiansPerSecond)
                    < Math.toRadians(ControlConstants.AT_ROTATIONAL_SPEED_THRESHOLD);
        }

        public boolean isAtRobotOrientedSpeeds(
                ChassisSpeeds targetChassisSpeeds, boolean includeRotation) {
            return (!includeRotation
                            || isAtRotationVelocity(targetChassisSpeeds.omegaRadiansPerSecond))
                    && Math.abs(
                                    targetChassisSpeeds.vxMetersPerSecond
                                            - chassisSpeeds.vxMetersPerSecond)
                            < ControlConstants.AT_TRANSLATIONAL_SPEED_THRESHOLD
                    && Math.abs(
                                    targetChassisSpeeds.vyMetersPerSecond
                                            - chassisSpeeds.vyMetersPerSecond)
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

    /**
     * @param x the x value for the translation joystick, positive being forward, in meters/sec
     * @param y the y value for the translation joystick, positive being left, in meters/sec
     */
    public record RobotOrientedTranslation(double x, double y) implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return status.isAtRobotOrientedSpeeds(new ChassisSpeeds(x, y, 0), false);
        }
    }

    /**
     * @param x the x value for the translation joystick, positive being forward, in meters/sec
     * @param y the y value for the translation joystick, positive being left, in meters/sec
     */
    public record FieldOrientedTranslation(double x, double y) implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return status.isAtFieldOrientedSpeeds(new ChassisSpeeds(x, y, 0), false);
        }
    }

    /**
     * @param omegaRadiansPerSecond the turn value from the rotation joystick, positive being CCW, in radians/sec
     */
    public record RotationVelocity(double omegaRadiansPerSecond) implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return status.isAtRotationVelocity(omegaRadiansPerSecond);
        }
    }

    /**
     * @param target rotational target as a Rotation2d
     */
    public record RotationTarget(Rotation2d target) implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return status.isAtRotationVelocity(0.0) && status.isAtRotationHeading(target);
        }
    }

    /**
     * Stops the drive motors and turns wheels in a cross formation to prevent robot from moving.
     */
    public record SetCross() implements Request<DriveStatus> {
        @Override
        public boolean isDone(DriveStatus status) {
            return status.isCrossed();
        }
    }

    public class Translation extends Mechanism<DriveStatus> {
        @Override
        public Request<DriveStatus> getIdleRequest() {
            return new SetCross();
        }
    }

    public final Translation translation = new Translation();

    public class Rotation extends Mechanism<DriveStatus> {
        @Override
        public Request<DriveStatus> getIdleRequest() {
            return new SetCross();
        }
    }

    public final Rotation rotation = new Rotation();

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

    private Translation2d[] wheelPositions;
    private SwerveDriveKinematics swerveDriveKinematics;

    private PIDController rotationPID;

    public final PPHolonomicDriveController controller;

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
        swerveFR =
                new SwerveModule(
                        "FR",
                        driveFR,
                        steerFR,
                        encoderFR,
                        config.driveMotorCurrentLimit(),
                        config.steerMotorCurrentLimit());
        swerveFL =
                new SwerveModule(
                        "FL",
                        driveFL,
                        steerFL,
                        encoderFL,
                        config.driveMotorCurrentLimit(),
                        config.steerMotorCurrentLimit());
        swerveBR =
                new SwerveModule(
                        "BR",
                        driveBR,
                        steerBR,
                        encoderBR,
                        config.driveMotorCurrentLimit(),
                        config.steerMotorCurrentLimit());
        swerveBL =
                new SwerveModule(
                        "BL",
                        driveBL,
                        steerBL,
                        encoderBL,
                        config.driveMotorCurrentLimit(),
                        config.steerMotorCurrentLimit());

        // Sets up odometry
        gyro = RobotProvider.instance.getGyro(DRIVE_GYRO);

        rotationPID = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_ROTATION_PID);

        MotorController[] motorList = new MotorController[] {driveFR, driveFL, driveBR, driveBL};
        CANcoder[] encoderList = new CANcoder[] {encoderFR, encoderFL, encoderBR, encoderBL};
        double halfDistanceBetweenWheels = config.distanceBetweenWheels() / 2;
        this.wheelPositions =
                new Translation2d[] {
                    getPositionForWheel(config.frontRightLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.frontLeftLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.backRightLocation(), halfDistanceBetweenWheels),
                    getPositionForWheel(config.backLeftLocation(), halfDistanceBetweenWheels)
                };

        swerveDriveKinematics = new SwerveDriveKinematics(wheelPositions);

        log("MotorList Length: " + motorList.length);
        log("CANCoderList Length: " + encoderList.length);
        swerveOdometry =
                new Odometry(
                        gyro,
                        motorList,
                        encoderList,
                        wheelPositions,
                        config.wheelCircumference(),
                        config.driveGearRatio(),
                        config.encoderToRevolutionConstant());

        double maxSpeed =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_MAX_MODULE_SPEED_MPS)
                        .valueOr(PathPlannerConstants.MAX_SPEED_MPS);

        double translationP =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_TRANSLATION_P)
                        .valueOr(PathPlannerConstants.TRANSLATION_P);
        double translationI =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_TRANSLATION_I)
                        .valueOr(PathPlannerConstants.TRANSLATION_I);
        double translationD =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_TRANSLATION_D)
                        .valueOr(PathPlannerConstants.TRANSLATION_D);
        double rotationP =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_ROTATION_P)
                        .valueOr(PathPlannerConstants.ROTATION_P);
        double rotationI =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_ROTATION_I)
                        .valueOr(PathPlannerConstants.ROTATION_I);
        double rotationD =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_ROTATION_D)
                        .valueOr(PathPlannerConstants.ROTATION_D);

        double maxWheelDistToCenter =
                Arrays.stream(wheelPositions)
                        .mapToDouble(Translation2d::getNorm)
                        .max()
                        .getAsDouble();

        controller =
                new PPHolonomicDriveController(
                        new PIDConstants(translationP, translationI, translationD),
                        new PIDConstants(rotationP, rotationI, rotationD),
                        maxSpeed,
                        maxWheelDistToCenter);
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

    private boolean shouldCrossWheels() {
        return rotation.getRequest() instanceof SetCross
                && translation.getRequest() instanceof SetCross;
    }

    @Override
    protected void run() {
        if (shouldCrossWheels()) {
            swerveFR.stopDrive();
            swerveFL.stopDrive();
            swerveBR.stopDrive();
            swerveBL.stopDrive();
            swerveFR.steer(config.frontRightLocation());
            swerveFL.steer(config.frontLeftLocation());
            swerveBR.steer(config.backRightLocation());
            swerveBL.steer(config.backLeftLocation());
            return;
        }

        double turn;
        if (rotation.getRequest() instanceof RotationTarget g) {
            rotationPID.setSetpoint(g.target().getDegrees());
            rotationPID.calculate(gyro.getAngle());
            turn = rotationPID.getOutput();
        } else if (rotation.getRequest() instanceof RotationVelocity g) {
            turn = g.omegaRadiansPerSecond();
        } else {
            throw new IllegalArgumentException();
        }

        double translationX;
        double translationY;
        if (translation.getRequest() instanceof RobotOrientedTranslation g) {
            translationX = g.x();
            translationY = g.y();
        } else if (translation.getRequest() instanceof FieldOrientedTranslation g) {
            final Optional<Alliance> alliance = DriverStation.getAlliance();
            final double yawRad =
                    Math.toRadians(
                            gyro.getAngle()
                                    + (alliance.isPresent() && alliance.get() == Alliance.Blue
                                            ? 0
                                            : 180));
            // Applies a rotational translation to controlRobotOriented
            // Counteracts the forward direction changing when the robot turns
            // TODO: change to inverse rotation matrix (rather than negative angle)
            translationX = Math.cos(-yawRad) * g.x() - Math.sin(-yawRad) * g.y();
            translationY = Math.sin(-yawRad) * g.x() + Math.cos(-yawRad) * g.y();
        } else {
            throw new IllegalArgumentException();
        }

        // Calculate the necessary turn velocity (m/s) for each motor:
        final double turnVelocity = config.wheelDistanceFromCenter() * turn;

        // Finds the vectors for turning and for translation of each module, and adds them
        // Applies this for each module
        swerveFR.driveAndSteer(
                new Vector2D(translationX, translationY)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.frontRightLocation()).normalize()));
        swerveFL.driveAndSteer(
                new Vector2D(translationX, translationY)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.frontLeftLocation()).normalize()));
        swerveBR.driveAndSteer(
                new Vector2D(translationX, translationY)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.backRightLocation()).normalize()));
        swerveBL.driveAndSteer(
                new Vector2D(translationX, translationY)
                        .add(
                                turnVelocity,
                                createOrthogonalVector(config.backLeftLocation()).normalize()));
    }

    /**
     * Resets gyro to zero degrees relative to the driver
     * Sets to 180 degrees if the driver is on red (facing backwards)
     */
    public void resetGyro() {
        rotation.checkContextReservation();
        final Optional<Alliance> alliance = DriverStation.getAlliance();
        resetGyro(alliance.isPresent() && alliance.get() == Alliance.Blue ? 0 : 180);
    }

    /**
     * Sets gyro to value in degrees
     * @param angle in degrees
     */
    public void resetGyro(double angle) {
        rotation.checkContextReservation();
        gyro.setAngle(angle);
    }

    public void setCurrentPosition(Pose2d P) {
        translation.checkContextReservation();
        rotation.checkContextReservation();
        // log("setCurrentPosition(): " + P);
        swerveOdometry.setCurrentPosition(P);
    }

    public void resetCurrentPosition() {
        translation.checkContextReservation();
        rotation.checkContextReservation();
        swerveOdometry.setCurrentPosition(new Pose2d());
    }

    private static Translation2d getPositionForWheel(
            Vector2D relativeLocation, double halfDistance) {
        return new Translation2d(
                relativeLocation.getX() * halfDistance, relativeLocation.getY() * halfDistance);
    }

    // Odometry
    @Override
    public DriveStatus reportStatus() {
        swerveOdometry.run();

        final double heading = gyro.getAngle();
        final double pitch = gyro.getPitch();
        final double roll = gyro.getRoll();
        final Pose2d currentPosition = swerveOdometry.getCurrPosition();

        final ChassisSpeeds chassisSpeeds =
                swerveDriveKinematics.toChassisSpeeds(
                        swerveFR.getModuleState(),
                        swerveFL.getModuleState(),
                        swerveBR.getModuleState(),
                        swerveBL.getModuleState());

        swerveFR.dashboardCurrentUsage();
        swerveFL.dashboardCurrentUsage();
        swerveBR.dashboardCurrentUsage();
        swerveBL.dashboardCurrentUsage();

        SwerveModuleState[] swerveStates =
                new SwerveModuleState[] {
                    swerveFR.getModuleState(),
                    swerveFL.getModuleState(),
                    swerveBR.getModuleState(),
                    swerveBL.getModuleState(),
                };

        return new DriveStatus(
                shouldCrossWheels(),
                heading,
                pitch,
                roll,
                currentPosition,
                chassisSpeeds,
                swerveStates);
    }
}
