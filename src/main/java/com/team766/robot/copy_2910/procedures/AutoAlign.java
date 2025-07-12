package com.team766.robot.copy_2910.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.Vision;
import edu.wpi.first.math.geometry.Pose2d;

public class AutoAlign extends Procedure {
    private Pose2d targetPosition;
    private SwerveDrive drive;
    private PIDController pidControllerX;
    private PIDController pidControllerY;
    private PIDController pidControllerRotation;

    public AutoAlign(Pose2d targetPosition, SwerveDrive drive) {
        this.targetPosition = targetPosition;
        this.drive = reserve(drive);
        pidControllerX = PIDController.loadFromConfig("DRIVE_X_PID");
        pidControllerY = PIDController.loadFromConfig("DRIVE_Y_PID");
        pidControllerRotation =
                PIDController.loadFromConfig("DRIVE_ROTATION_PID");
    }

    public AutoAlign(Pose2d targetPosition, double threshold, SwerveDrive drive) {
        this(targetPosition, drive);
        pidControllerX.setThreshold(threshold);
        pidControllerY.setThreshold(threshold);
    }

    public void run(Context context) {
        Pose2d currentPosition;
        try {
            currentPosition = getStatusOrThrow(Vision.VisionStatus.class).getApriltagPose2d();
        } catch (Exception e) {
            drive.stopDrive();
            log("No valid pose found from vision: " + e.getMessage());
            return; // Exit if no valid pose is found
        }
        // currentHeading = getStatusOrThrow(SwerveDrive.DriveStatus.class).heading()
        double currentHeading = 0;

        pidControllerX.setSetpoint(targetPosition.getX());
        pidControllerY.setSetpoint(targetPosition.getY());
        double correctedTargetDegrees =
                targetPosition.getRotation().getDegrees()
                        + 360
                                * Math.round(
                                        (currentHeading - targetPosition.getRotation().getDegrees())
                                                / 360);
        pidControllerRotation.setSetpoint(correctedTargetDegrees);
        while (!pidControllerX.isDone()
                || !pidControllerY.isDone()
                || !pidControllerRotation.isDone()) {
            try {
                currentPosition = getStatusOrThrow(Vision.VisionStatus.class).getApriltagPose2d();
            } catch (Exception e) {
                log("No valid pose found from vision: " + e.getMessage());
                drive.stopDrive();
                return; // Exit if no valid pose is found
            }
            // currentHeading = getStatusOrThrow(SwerveDrive.DriveStatus.class).heading()
            currentHeading = 0;

            pidControllerX.calculate(currentPosition.getX());
            pidControllerY.calculate(currentPosition.getY());
            pidControllerRotation.calculate(currentHeading);

            drive.controlFieldOriented(
                    pidControllerX.isDone() ? 0 : pidControllerX.getOutput(),
                    pidControllerY.isDone() ? 0 : pidControllerY.getOutput(),
                    pidControllerRotation.isDone() ? 0 : pidControllerRotation.getOutput());
            context.yield();
        }
        drive.stopDrive();
    }
}
