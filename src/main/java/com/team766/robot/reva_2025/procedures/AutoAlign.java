package com.team766.robot.reva_2025.procedures;

import com.team766.controllers.PIDController;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.reva_2025.Robot;
import edu.wpi.first.math.geometry.Pose2d;

public class AutoAlign extends Procedure {
    public Pose2d targetPosition;

    public AutoAlign(Pose2d targetPosition) {
        this.targetPosition = targetPosition;
    }
    public void run(Context context) {
        double P = 0;
        double I = 0;
        double D = 0;
        double threshold = 0.01;
        PIDController pidControllerX = new PIDController(P, I, D, 0, 1, threshold);
        PIDController pidControllerY = new PIDController(P, I, D, 0, 1, threshold);
        PIDController pidControllerRotation = new PIDController(P, I, D, -1, 1, threshold);
        Pose2d currentPosition;
        double currentHeading;
        double motorX;
        double motorY; 
        double motorRotation;  
        
        context.takeOwnership(Robot.drive);
        pidControllerX.setSetpoint(targetPosition.getX());
        pidControllerY.setSetpoint(targetPosition.getY());
        pidControllerRotation.setSetpoint(targetPosition.getRotation().getDegrees());
        while(!pidControllerX.isDone() || !pidControllerY.isDone() || !pidControllerRotation.isDone()) {
            currentPosition = Robot.drive.getCurrentPosition();
            currentHeading = Robot.drive.getHeading();
            pidControllerX.calculate(currentPosition.getX());
            pidControllerY.calculate(currentPosition.getY());
            pidControllerRotation.calculate(currentHeading);
            motorX = pidControllerX.getOutput();
            motorY = pidControllerY.getOutput();
            motorRotation = pidControllerRotation.getOutput();
            Robot.drive.controlFieldOriented(motorX, motorY, motorRotation);
        }
        Robot.drive.stopDrive();
        Robot.drive.setCross();
    } 

}


