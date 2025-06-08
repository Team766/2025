package com.team766.robot.reva_2025.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.ConfigConstants;
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
        pidControllerX = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_TRANSLATION_PID);
        pidControllerY = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_TRANSLATION_PID);
        pidControllerRotation =
                PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_ROTATION_PID);
    }

    public AutoAlign(Pose2d targetPosition, double threshold, SwerveDrive drive) {
        this(targetPosition, drive);
        pidControllerX.setThreshold(threshold);
        pidControllerY.setThreshold(threshold);
    }

    public void run(Context context) {
        Pose2d currentPosition;
        double currentHeading = getStatusOrThrow(SwerveDrive.DriveStatus.class).heading();

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
            currentPosition = getStatusOrThrow(SwerveDrive.DriveStatus.class).currentPosition();
            currentHeading = getStatusOrThrow(SwerveDrive.DriveStatus.class).heading();

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
