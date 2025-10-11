package com.team766.robot.common.mechanisms;

import static com.team766.math.Maths.normalizeAngleDegrees;
import static com.team766.robot.common.constants.ConfigConstants.*;

import com.team766.controllers.PIDController;
import com.team766.framework.MultiFacetedMechanismWithStatus;
import com.team766.framework.NoReservationRequired;
import com.team766.framework.Status;
import com.team766.framework.StatusBus;
import com.team766.hal.EncoderReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SwerveDrive extends MultiFacetedMechanismWithStatus<SwerveDrive.DriveStatus> {
    /**
     * @param heading current heading in degrees
     */
    public static record DriveStatus(
            boolean isCrossed,
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

        public boolean isBalanced() {
            return Math.toDegrees(
                            Math.acos(
                                    Math.cos(
                                            Math.toRadians(roll)
                                                    * Math.cos(Math.toRadians(pitch)))))
                    < ControlConstants.ROBOT_BALANCED_ANGLE;
        }
    }

    public class Translation extends MechanismFacet {
        private enum VelocityReference {
            ROBOT,
            FIELD,
            ALLIANCE,
        }

        private double commandedVelocityX = 0;
        private double commandedVelocityY = 0;
        private VelocityReference commandedVelocityReference = VelocityReference.ROBOT;
        Translation2d targetRobotVelocity = Translation2d.kZero;

        /**
         * @param x the x value for the translation joystick, positive being forward, in meters/sec
         * @param y the y value for the translation joystick, positive being left, in meters/sec
         */
        public void controlRobotOriented(double x, double y) {
            targetRobotVelocity = new Translation2d(x, y);
            commandedVelocityReference = VelocityReference.ROBOT;
        }

        /**
         * @param x the x value for the translation joystick, positive being forward, in meters/sec
         * @param y the y value for the translation joystick, positive being left, in meters/sec
         */
        public void controlAllianceOriented(double x, double y) {
            commandedVelocityX = x;
            commandedVelocityY = y;
            commandedVelocityReference = VelocityReference.ALLIANCE;
        }

        /**
         * @param x the x value for the translation joystick, positive being forward, in meters/sec
         * @param y the y value for the translation joystick, positive being left, in meters/sec
         */
        public void controlFieldOriented(double x, double y) {
            commandedVelocityX = x;
            commandedVelocityY = y;
            commandedVelocityReference = VelocityReference.FIELD;
        }

        /**
         * Stops the drive translating.
         */
        public void stop() {
            targetRobotVelocity = Translation2d.kZero;
            commandedVelocityReference = VelocityReference.ROBOT;
        }

        @Override
        protected void onMechanismIdle() {
            stop();
        }

        @Override
        protected void run() {
            switch (commandedVelocityReference) {
                case ROBOT -> {}
                case ALLIANCE -> {
                    final Optional<Alliance> alliance = DriverStation.getAlliance();
                    final double yawRad =
                            Math.toRadians(
                                    getStatus().heading()
                                            + (alliance.isPresent()
                                                            && alliance.get() == Alliance.Blue
                                                    ? 0
                                                    : 180));

                    // Applies a rotational translation to controlRobotOriented
                    // Counteracts the forward direction changing when the robot turns
                    targetRobotVelocity =
                            new Translation2d(
                                    Math.cos(-yawRad) * commandedVelocityX
                                            - Math.sin(-yawRad) * commandedVelocityY,
                                    Math.sin(-yawRad) * commandedVelocityX
                                            + Math.cos(-yawRad) * commandedVelocityY);
                }
                case FIELD -> {
                    final double yawRad = Math.toRadians(getStatus().heading());

                    // Applies a rotational translation to controlRobotOriented
                    // Counteracts the forward direction changing when the robot turns
                    targetRobotVelocity =
                            new Translation2d(
                                    Math.cos(-yawRad) * commandedVelocityX
                                            - Math.sin(-yawRad) * commandedVelocityY,
                                    Math.sin(-yawRad) * commandedVelocityX
                                            + Math.cos(-yawRad) * commandedVelocityY);
                }
            }
        }
    }

    public final Translation translation = addFacet(new Translation());

    public class Rotation extends MechanismFacet {
        private PIDController rotationPID =
                PIDController.loadFromConfig(ConfigConstants.TARGET_LOCK_ROTATION_PID);
        private boolean pidActive = false;
        double targetVelocity = 0.0;

        /**
         * @param omegaRadiansPerSecond the turn value from the rotation joystick, positive being CCW, in radians/sec
         */
        public void controlVelocity(double omegaRadiansPerSecond) {
            targetVelocity = omegaRadiansPerSecond;
            pidActive = false;
        }

        /**
         * @param target rotational target as a Rotation2d in alliance-oriented coordinates.
         */
        public void controlHeading(Rotation2d target) {
            if (!pidActive) {
                rotationPID.reset();
            }
            rotationPID.setSetpoint(target.getDegrees());
            pidActive = true;
        }

        /**
         * Stops the drive rotating.
         */
        public void stop() {
            targetVelocity = 0.0;
            pidActive = false;
        }

        @Override
        protected void onMechanismIdle() {
            stop();
        }

        @Override
        protected void run() {
            if (pidActive) {
                rotationPID.calculate(getStatus().heading());
                targetVelocity =
                        Math.abs(rotationPID.getOutput())
                                        < ControlConstants.DEFAULT_ROTATION_THRESHOLD
                                ? 0
                                : rotationPID.getOutput();
            }
        }
    }

    public final Rotation rotation = addFacet(new Rotation());

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

    private double[] prevCamTimes;

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
        EncoderReader encoderFR = RobotProvider.instance.getEncoder(DRIVE_ENCODER_FRONT_RIGHT);
        EncoderReader encoderFL = RobotProvider.instance.getEncoder(DRIVE_ENCODER_FRONT_LEFT);
        EncoderReader encoderBR = RobotProvider.instance.getEncoder(DRIVE_ENCODER_BACK_RIGHT);
        EncoderReader encoderBL = RobotProvider.instance.getEncoder(DRIVE_ENCODER_BACK_LEFT);

        // initialize the swerve modules
        swerveFR = new SwerveModule("FR", driveFR, steerFR, encoderFR, config);
        swerveFL = new SwerveModule("FL", driveFL, steerFL, encoderFL, config);
        swerveBR = new SwerveModule("BR", driveBR, steerBR, encoderBR, config);
        swerveBL = new SwerveModule("BL", driveBL, steerBL, encoderBL, config);

        // Sets up odometry
        gyro = RobotProvider.instance.getGyro(DRIVE_GYRO);

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

        swerveOdometry = new Odometry(gyro, moduleList);

        kalmanFilter = new KalmanFilter();

        prevCamTimes = new double[4];
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
    private static Translation2d createOrthogonalUnitVector(Translation2d vector) {
        return new Translation2d(-vector.getY(), vector.getX()).div(vector.getNorm());
    }

    private boolean shouldCrossWheels() {
        return rotation.targetVelocity == 0.0
                && translation.targetRobotVelocity.equals(Translation2d.kZero);
    }

    @Override
    protected void run() {
        if (shouldCrossWheels()) {
            swerveFR.stopDrive();
            swerveFL.stopDrive();
            swerveBR.stopDrive();
            swerveBL.stopDrive();
            swerveFR.steer(config.frontRightLocation().getAngle());
            swerveFL.steer(config.frontLeftLocation().getAngle());
            swerveBR.steer(config.backRightLocation().getAngle());
            swerveBL.steer(config.backLeftLocation().getAngle());
            return;
        }

        final double turn = rotation.targetVelocity;
        final Translation2d translationVelocity = translation.targetRobotVelocity;

        SmartDashboard.putString(
                "Swerve Commands",
                "x: "
                        + translationVelocity.getX()
                        + ", y: "
                        + translationVelocity.getY()
                        + ", turn: "
                        + turn);

        // Calculate the necessary turn velocity (m/s) for each motor:
        final double turnVelocity = config.wheelDistanceFromCenter() * turn;

        // Finds the vectors for turning and for translation of each module, and adds them
        // Applies this for each module
        swerveFR.driveAndSteer(
                translationVelocity.plus(
                        createOrthogonalUnitVector(config.frontRightLocation())
                                .times(turnVelocity)));
        swerveFL.driveAndSteer(
                translationVelocity.plus(
                        createOrthogonalUnitVector(config.frontLeftLocation())
                                .times(turnVelocity)));
        swerveBR.driveAndSteer(
                translationVelocity.plus(
                        createOrthogonalUnitVector(config.backRightLocation())
                                .times(turnVelocity)));
        swerveBL.driveAndSteer(
                translationVelocity.plus(
                        createOrthogonalUnitVector(config.backLeftLocation()).times(turnVelocity)));
    }

    /**
     * Maps parameters to robot oriented swerve movement
     * Sets robot to manual control mode rather than a rotation setpoint
     * @param x the x value for the translation joystick, positive being forward
     * @param y the y value for the translation joystick, positive being left
     * @param turn the turn value from the rotation joystick, positive being CCW
     */
    public void controlRobotOriented(double x, double y, double turn) {
        translation.controlRobotOriented(x, y);
        rotation.controlVelocity(turn);
    }

    /**
     * Overloads controlRobotOriented to work with a chassisSpeeds input
     */
    public void controlRobotOriented(ChassisSpeeds chassisSpeeds) {
        controlRobotOriented(
                chassisSpeeds.vxMetersPerSecond,
                chassisSpeeds.vyMetersPerSecond,
                chassisSpeeds.omegaRadiansPerSecond);
    }

    /**
     * Maps parameters to robot movement relative to the field
     * Sets robot to manual control mode rather than a rotation setpoint
     * @param x the x value for the position joystick, positive being forward, in meters/sec
     * @param y the y value for the position joystick, positive being left, in meters/sec
     * @param turn the turn value from the rotation joystick, positive being CCW, in radians/sec
     */
    public void controlFieldOriented(double x, double y, double turn) {
        translation.controlFieldOriented(x, y);
        rotation.controlVelocity(turn);
    }

    /**
     * Overloads controlFieldOriented to work with a chassisSpeeds input
     */
    public void controlFieldOriented(ChassisSpeeds chassisSpeeds) {
        controlFieldOriented(
                chassisSpeeds.vxMetersPerSecond,
                chassisSpeeds.vyMetersPerSecond,
                chassisSpeeds.omegaRadiansPerSecond);
    }

    /**
     * Maps parameters to robot movement relative to the ALLIANCE
     * Sets robot to manual control mode rather than a rotation setpoint
     * @param x the x value for the position joystick, positive being forward, in meters/sec
     * @param y the y value for the position joystick, positive being left, in meters/sec
     * @param turn the turn value from the rotation joystick, positive being CCW, in radians/sec
     */
    public void controlAllianceOriented(double x, double y, double turn) {
        translation.controlAllianceOriented(x, y);
        rotation.controlVelocity(turn);
    }

    /**
     * Overloads controlAllianceOriented to work with a chassisSpeeds input
     * @param chassisSpeeds
     */
    public void controlAllianceOriented(ChassisSpeeds chassisSpeeds) {
        controlAllianceOriented(
                chassisSpeeds.vxMetersPerSecond,
                chassisSpeeds.vyMetersPerSecond,
                chassisSpeeds.omegaRadiansPerSecond);
    }

    /**
     * Allows for alliance oriented control of the robot's position while moving to a specific angle for rotation
     * @param x the x value for the position joystick, positive being forward
     * @param y the y value for the position joystick, positive being left
     * @param target rotational target as a Rotation2d, can input a null value
     */
    public void controlAllianceOrientedWithRotationTarget(double x, double y, Rotation2d target) {
        translation.controlAllianceOriented(x, y);
        if (target != null) {
            rotation.controlHeading(target);
        }
    }

    @NoReservationRequired
    public SwerveConfig getSwerveConfig() {
        return config;
    }

    /*
     * Stops each drive motor and turns wheels in a cross formation to prevent robot from moving
     */
    public void stopDrive() {
        translation.stop();
        rotation.stop();
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
        kalmanFilter.resetPos();
    }

    private static Translation2d getPositionForWheel(
            Translation2d relativeLocation, double halfDistance) {
        return new Translation2d(
                relativeLocation.getX() * halfDistance, relativeLocation.getY() * halfDistance);
    }

    // Odometry
    @Override
    protected DriveStatus updateStatus() {
        kalmanFilter.addOdometryInput(
                swerveOdometry.calculateCurrentPositionChange(),
                RobotProvider.instance.getClock().getTime());

        final double heading = gyro.getAngle();
        final double pitch = gyro.getPitch();
        final double roll = gyro.getRoll();

        var visionStatus = StatusBus.getInstance().getStatus(Vision.VisionStatus.class);
        if (visionStatus.isPresent() && !visionStatus.get().allTags().isEmpty()) {
            int camCounter = 0;
            for (List<TimestampedApriltag> cameraTags : visionStatus.get().allTags()) {
                camCounter++;
                HashMap<Translation2d, Double> tagPoses = new HashMap<>();
                if (cameraTags.size() > 0) {
                    for (TimestampedApriltag tag : cameraTags) {
                        Translation2d position =
                                tag.toRobotPosition(Rotation2d.fromDegrees(heading));
                        tagPoses.put(position, tag.pose3d().getTranslation().getNorm());
                        if (Logger.isLoggingToDataLog()) {
                            org.littletonrobotics.junction.Logger.recordOutput(
                                    "Vision Pos/cam " + camCounter + "/tagID " + tag.tagId(),
                                    new Pose2d(position, Rotation2d.fromDegrees(heading)));
                        }
                    }

                    if (Logger.isLoggingToDataLog()) {
                        org.littletonrobotics.junction.Logger.recordOutput(
                                "delay",
                                RobotProvider.instance.getClock().getTime()
                                        - (cameraTags.get(0).collectTime() / 1000000.));
                    }

                    // Only do position update if current timestamp doesn't match with previous
                    // timestamp
                    if (prevCamTimes[0] != 0
                            && Math.abs(
                                            cameraTags.get(0).collectTime()
                                                    - prevCamTimes[camCounter - 1])
                                    > 1 // microseconds
                    ) {
                        kalmanFilter.updateWithVisionMeasurement(
                                tagPoses,
                                cameraTags.get(0).covariance(),
                                RobotProvider.instance
                                        .getClock()
                                        .getTime()); // Latency correction off
                        // cameraTags.get(0).collectTime() / 1000000.); // Latency correction option
                    }
                    prevCamTimes[camCounter - 1] = cameraTags.get(0).collectTime();
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
                shouldCrossWheels(),
                heading,
                pitch,
                roll,
                currentPosition,
                robotOrientedChassisSpeeds,
                fieldOrientedChassisSpeeds,
                swerveModuleStates);
    }
}
