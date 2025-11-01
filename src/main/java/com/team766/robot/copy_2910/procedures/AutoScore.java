package com.team766.robot.copy_2910.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Vision;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import edu.wpi.first.math.geometry.Pose2d;

public class AutoScore extends Procedure {
    private Pose2d targetPosition;
    private SwerveDrive drive;
    private PIDController pidControllerX;
    private PIDController pidControllerY;
    private PIDController pidControllerRotation;
    private Wrist wrist;
    private Shoulder shoulder;
    private Elevator elevator;

    private double elevatorHeight;
    private double wristAngle;
    private double shoulderAngle;

    public AutoScore(
            Pose2d targetPosition,
            SwerveDrive drive,
            Wrist wrist,
            Shoulder shoulder,
            Elevator elevator,
            double elevatorHeight,
            double wristAngle,
            double shoulderAngle) {
        this.targetPosition = targetPosition;
        this.drive = reserve(drive);
        this.wrist = reserve(wrist);
        this.shoulder = reserve(shoulder);
        this.elevator = reserve(elevator);
        this.elevatorHeight = elevatorHeight;
        this.wristAngle = wristAngle;
        this.shoulderAngle = shoulderAngle;
        pidControllerX = PIDController.loadFromConfig("DRIVE_X_PID");
        pidControllerY = PIDController.loadFromConfig("DRIVE_Y_PID");
        pidControllerRotation = PIDController.loadFromConfig("DRIVE_ROTATION_PID");
    }

    public void run(Context context) {
        shoulder.setSetpoint(shoulderAngle);
        wrist.setSetpoint(wristAngle);
        elevator.setPosition(elevatorHeight);
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

            drive.controlRobotOriented(
                    pidControllerX.isDone() ? 0 : pidControllerX.getOutput(),
                    pidControllerY.isDone() ? 0 : pidControllerY.getOutput(),
                    pidControllerRotation.isDone() ? 0 : pidControllerRotation.getOutput());
            context.yield();
        }
        drive.stopDrive();
    }
}
