package com.team766.robot.reva_2025.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.Robot;
import edu.wpi.first.math.geometry.Pose2d;

public class AutoAlign extends Procedure {
    private Pose2d targetPosition;
    private SwerveDrive drive;
    private PIDController pidControllerX;
    private PIDController pidControllerY;
    private PIDController pidControllerRotation;
    private double P_translation;
    private double I_translation;
    private double D_translation;
    private double P_rotation;
    private double I_rotation;
    private double D_rotation;
    private double threshold_translation;
    private double threshold_rotation; 

    public AutoAlign(Pose2d targetPosition, SwerveDrive drive) {
        this.targetPosition = targetPosition;
        this.drive = drive;
        this.P_translation = 0;
        this.I_translation = 0;
        this.D_translation = 0;
        this.P_rotation = 0;
        this.I_rotation = 0;
        this.D_rotation = 0;
        this.threshold = 0.01;
        this.threshold_translation = 0.01;
        this.threshold_rotation = 0.01;
        this.pidControllerX = new PIDController(this.P_translation, this.I_translation, this.D_translation, 0, 1, this.threshold_translation);
        this.pidControllerY = new PIDController(this.P_translation, this.I_translation, this.D_translation, 0, 1, this.threshold_translation);
        this.pidControllerRotation = new PIDController(this.P_rotation, this.I_rotation, this.D_rotation, -1, 1, this.threshold_rotation);  
    }
    public void run(Context context) {
        Pose2d currentPosition;
        double currentHeading;  

        context.takeOwnership(this.drive);
        this.pidControllerX.setSetpoint(targetPosition.getX());
        this.pidControllerY.setSetpoint(targetPosition.getY());
        this.pidControllerRotation.setSetpoint(targetPosition.getRotation().getDegrees());
        while (!this.pidControllerX.isDone() || !this.pidControllerY.isDone() || !this.pidControllerRotation.isDone()) {
            currentPosition = this.drive.getCurrentPosition();
            currentHeading = this.drive.getHeading();
            this.pidControllerX.calculate(currentPosition.getX());
            this.pidControllerY.calculate(currentPosition.getY());
            this.pidControllerRotation.calculate(currentHeading);
            this.drive.controlFieldOriented(this.pidControllerX.getOutput(), this.pidControllerY.getOutput(), this.pidControllerRotation.getOutput());
            context.yield();
        }
        this.drive.stopDrive();
        this.drive.setCross();
    } 

}


