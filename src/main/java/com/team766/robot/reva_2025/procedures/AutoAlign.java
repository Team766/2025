package com.team766.robot.reva_2025.procedures;

import com.team766.config.ConfigFileReader;
import com.team766.controllers.PIDController;
import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.ConfigConstants;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class AutoAlign extends Procedure {
    private Pose2d targetPosition;
    private SwerveDrive drive;
    private PIDController pidControllerTranslaton;
    private PIDController pidControllerRotation;
    private double vectorAngleThreshold;

    public AutoAlign(Pose2d targetPosition, SwerveDrive drive) {
        this.targetPosition = targetPosition;
        this.drive = reserve(drive);
        pidControllerTranslaton =
                PIDController.loadFromConfig(
                        ConfigConstants.DRIVE_TARGET_TRANSLATION_PID);
        pidControllerRotation =
                PIDController.loadFromConfig(ConfigConstants.DRIVE_TARGET_ROTATION_PID);
        
        vectorAngleThreshold = ConfigFileReader.instance.getDouble(ConfigConstants.DRIVE_TARGET_VECTOR_ANGLE_THRESHOLD).get();
    }

    private Translation2d getCurrentDistToTarget() {
        return targetPosition
        .minus(
                getStatusOrThrow(SwerveDrive.DriveStatus.class)
                        .currentPosition())
        .getTranslation();
    }

    private double getCurrentHeading() {
        return Math.toRadians(getStatusOrThrow(SwerveDrive.DriveStatus.class).headingDeg());
    }

    public void run(Context context) {
        // need to initialize so vectorAngle still works even if starting close to the point
        Translation2d currentDistToTarget = getCurrentDistToTarget();
        Rotation2d vectorAngle = currentDistToTarget.getAngle();
        double currentHeading = getCurrentHeading();

        // calculates with distance away from setpoint, so the target is 0
        pidControllerTranslaton.setSetpoint(0); 
        pidControllerRotation.setSetpoint(targetPosition.getRotation().getRadians());
        while (!pidControllerTranslaton.isDone()
                || !pidControllerRotation.isDone()) {

            currentDistToTarget = getCurrentDistToTarget();
            currentHeading = getCurrentHeading();
                    

            if (!pidControllerTranslaton.isDone()) {
                // error is magnitude of distance to target point
                pidControllerTranslaton.calculate(currentDistToTarget.getNorm());
            }

            if (!pidControllerRotation.isDone()) {
                pidControllerRotation.calculate(currentHeading);
            }

            if (currentDistToTarget.getNorm() > vectorAngleThreshold) {
                vectorAngle = currentDistToTarget.getAngle();
            }

            Translation2d translationOutput =
                    new Translation2d(
                            pidControllerTranslaton.getOutput(),
                            vectorAngle);

            drive.controlFieldOriented(
                    translationOutput.getX(),
                    translationOutput.getY(),
                    pidControllerRotation.getOutput());
            context.yield();
        }
        drive.stopDrive();
    }
}
