package com.team766.robot.common.procedures;

import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.util.FlippingUtil;
import com.team766.config.ConfigFileReader;
import com.team766.framework.Context;
import com.team766.framework.FunctionalProcedure;
import com.team766.framework.Procedure;
import com.team766.logging.Logger;
import com.team766.robot.common.SwerveConfig;
import com.team766.robot.common.constants.ConfigConstants;
import com.team766.robot.common.constants.PathPlannerConstants;
import com.team766.robot.common.mechanisms.SwerveDrive;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public abstract class PathSequenceAuto extends Procedure {

    private final ArrayList<Procedure> pathItems;
    private final SwerveDrive drive;
    private final Pose2d initialPosition;
    private final RobotConfig robotConfig;
    private final PPHolonomicDriveController controller;

    /**
     * Sequencer for using path following with other procedures
     * @param drive The instantiation of drive for the robot (pass in Robot.drive)
     * @param initialPosition Starting position on Blue Alliance in meters (gets flipped when on red)
     */
    public PathSequenceAuto(SwerveDrive drive, Pose2d initialPosition) {
        pathItems = new ArrayList<Procedure>();
        this.drive = reserve(drive);
        this.robotConfig = createRobotConfig(drive);
        this.controller = createDriveController();
        this.initialPosition = initialPosition;
    }

    private static Translation2d wheelLocationAsTranslation(
            double distanceFromCenter, Translation2d vector) {
        vector = vector.div(vector.getNorm()).times(distanceFromCenter);
        return new Translation2d(vector.getX(), vector.getY());
    }

    private RobotConfig createRobotConfig(SwerveDrive drive) {
        SwerveConfig swerveConfig = drive.getSwerveConfig();

        // TODO: should max speed, mass, moment of inertia, etc be part of the SwerveConfig?
        double maxSpeed =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_MAX_MODULE_SPEED_MPS)
                        .valueOr(PathPlannerConstants.MAX_SPEED_MPS);

        double massKG =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_MASS_KG)
                        .valueOr(PathPlannerConstants.MASS_KG);

        double momentOfInertia =
                ConfigFileReader.getInstance()
                        .getDouble(ConfigConstants.PATH_FOLLOWING_MOMENT_OF_INERTIA)
                        .valueOr(PathPlannerConstants.MOMENT_OF_INTERTIA);

        // TODO: check numMotors is only counting drive motors
        // TODO: measure CoF, consider making part of SwerveConfig
        ModuleConfig moduleConfig =
                new ModuleConfig(
                        swerveConfig.wheelRadius() / 100.,
                        maxSpeed,
                        1.2 /* guess at CoF */,
                        swerveConfig.driveMotor(),
                        swerveConfig.driveMotorCurrentLimit(),
                        1 /* num motors */);

        RobotConfig robotConfig =
                new RobotConfig(
                        massKG,
                        momentOfInertia,
                        moduleConfig,
                        new Translation2d[] {
                            wheelLocationAsTranslation(
                                    swerveConfig.wheelDistanceFromCenter(),
                                    swerveConfig.frontLeftLocation()),
                            wheelLocationAsTranslation(
                                    swerveConfig.wheelDistanceFromCenter(),
                                    swerveConfig.frontRightLocation()),
                            wheelLocationAsTranslation(
                                    swerveConfig.wheelDistanceFromCenter(),
                                    swerveConfig.backLeftLocation()),
                            wheelLocationAsTranslation(
                                    swerveConfig.wheelDistanceFromCenter(),
                                    swerveConfig.backRightLocation())
                        });
        return robotConfig;
    }

    private PPHolonomicDriveController createDriveController() {
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

        return new PPHolonomicDriveController(
                new PIDConstants(translationP, translationI, translationD),
                new PIDConstants(rotationP, rotationI, rotationD));
    }

    /**
     * Add a path to the sequence.  Must be called in the constructor of the subclass.
     */
    protected void addPath(String pathName) {
        // TODO: should this log errors and otherwise proceed, or throw an exception and
        // somehow disable the entire auton (but not disable the robot completely, so teleop still
        // works)?
        try {
            pathItems.add(new FollowPath(pathName, controller, robotConfig, drive));
        } catch (Exception e) {
            Logger.get(Category.AUTONOMOUS)
                    .logRaw(
                            Severity.WARNING,
                            "!!!! Error loading path "
                                    + pathName
                                    + ": "
                                    + e.getMessage()
                                    + ", skipping in auton.");
        }
    }

    /**
     * Add a procedure to the sequence.  Must be called in the constructor of the subclass.
     */
    protected void addProcedure(Procedure procedure) {
        reserve(procedure.reservations()); // reserve any mechanisms (etc) this sub-procedure uses
        pathItems.add(procedure);
    }

    /**
     * Add a wait to the sequence.  Must be called in the constructor of the subclass.
     */
    protected void addWait(double waitForSeconds) {
        pathItems.add(
                new FunctionalProcedure(
                        Set.of(), (context) -> context.waitForSeconds(waitForSeconds)));
    }

    @Override
    public void run(Context context) {
        boolean shouldFlipAuton = false;
        Optional<Alliance> alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
            shouldFlipAuton = (alliance.get() == Alliance.Red);
        } else {
            log("Unable to get Alliance for auton " + this.getClass().getSimpleName());
            log("Cannot determine if we should flip auton.");
            log("Skipping auton");
            return;
        }

        // if (!visionSpeakerHelper.updateTarget(context)) {
        drive.setCurrentPosition(
                shouldFlipAuton ? FlippingUtil.flipFieldPose(initialPosition) : initialPosition);
        // }
        drive.resetGyro(
                (shouldFlipAuton
                                ? FlippingUtil.flipFieldRotation(initialPosition.getRotation())
                                : initialPosition.getRotation())
                        .getDegrees());
        for (Procedure pathItem : pathItems) {
            context.runSync(pathItem);
            context.yield();
        }
    }
}
