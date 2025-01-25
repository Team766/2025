package com.team766.robot.reva_2025.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
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
        pidControllerY = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_TRANSLATION_PID);
        pidControllerY = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_TRANSLATION_PID);
        pidControllerRotation = PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_ROTATION_PID);
    }

    public void run(Context context) {
        Pose2d currentPosition;
        double currentHeading;  

        pidControllerX.setSetpoint(targetPosition.getX());
        pidControllerY.setSetpoint(targetPosition.getY());
        pidControllerRotation.setSetpoint(targetPosition.getRotation().getDegrees());
        while (!pidControllerX.isDone() || !pidControllerY.isDone() || !pidControllerRotation.isDone()) {
            currentPosition = getStatusOrThrow(SwerveDrive.DriveStatus.class).currentPosition();
            currentHeading =  getStatusOrThrow(SwerveDrive.DriveStatus.class).heading();
            pidControllerX.calculate(currentPosition.getX());
            pidControllerY.calculate(currentPosition.getY());
            pidControllerRotation.calculate(currentHeading);
            drive.controlFieldOriented(pidControllerX.getOutput(), pidControllerY.getOutput(), pidControllerRotation.getOutput());
            context.yield();
        }
        drive.stopDrive();
    } 

}


