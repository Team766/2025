package com.team766.robot.common.procedures;

import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.trajectory.PathPlannerTrajectory;
import com.pathplanner.lib.trajectory.PathPlannerTrajectoryState;
import com.pathplanner.lib.util.FileVersionException;
import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Timer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import org.json.simple.parser.ParseException;

public class FollowPath extends Procedure {
    private PathPlannerPath path; // may be flipped
    private final PPHolonomicDriveController controller;
    private final RobotConfig config;
    private final SwerveDrive drive;
    private final Timer timer = new Timer();
    private PathPlannerTrajectory generatedTrajectory;

    public FollowPath(
            PathPlannerPath path,
            PPHolonomicDriveController controller,
            RobotConfig config,
            SwerveDrive drive) {
        this.path = path;
        this.controller = controller;
        this.config = config;
        this.drive = reserve(drive);
    }

    public FollowPath(
            String autoName,
            PPHolonomicDriveController controller,
            RobotConfig config,
            SwerveDrive drive)
            throws IOException, FileNotFoundException, ParseException, FileVersionException {
        this(PathPlannerPath.fromPathFile(autoName), controller, config, drive);
    }

    @Override
    public void run(Context context) {
        Optional<Alliance> alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
            boolean flip = (alliance.get() == Alliance.Red);
            if (flip) {
                path = path.flipPath();
            }
        } else {
            log("Unable to get Alliance in FollowPath.");
            // TODO: don't follow this path?
        }

        // intitialization

        var driveStatus = getStatusOrThrow(SwerveDrive.DriveStatus.class);
        Pose2d curPose = driveStatus.currentPosition();
        ChassisSpeeds currentSpeeds =
                driveStatus.robotOrientedChassisSpeeds(); // FIXME: MIGHT HAVE TO BE ABSOLUTE

        controller.reset(curPose, currentSpeeds);

        generatedTrajectory = path.generateTrajectory(currentSpeeds, curPose.getRotation(), config);

        timer.reset();
        timer.start();

        // execute
        log("time: " + generatedTrajectory.getTotalTimeSeconds());
        while (!timer.hasElapsed(generatedTrajectory.getTotalTimeSeconds())) {
            double currentTime = timer.get();
            PathPlannerTrajectoryState targetState = generatedTrajectory.sample(currentTime);
            driveStatus = getStatusOrThrow(SwerveDrive.DriveStatus.class);
            curPose = driveStatus.currentPosition();

            ChassisSpeeds targetSpeeds =
                    controller.calculateRobotRelativeSpeeds(curPose, targetState);

            org.littletonrobotics.junction.Logger.recordOutput(
                    "current heading", curPose.getRotation().getRadians());

            org.littletonrobotics.junction.Logger.recordOutput(
                    "input rotational velocity", targetSpeeds.omegaRadiansPerSecond);
            org.littletonrobotics.junction.Logger.recordOutput("targetPose", targetState.pose);
            drive.controlRobotOriented(targetSpeeds);
            context.yield();
        }

        if (path.getGoalEndState().velocity().magnitude() < 0.1) {
            drive.stopDrive();
        }
    }
}
