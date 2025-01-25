package com.team766.robot.reva_2025.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.CoralConstants.CoralConstant;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import edu.wpi.first.math.geometry.Pose2d;

public class ScoreCoral extends Procedure{

    private CoralConstant position;
    private double levelHeight;
    private double angle;
    private SwerveDrive drive;
    private Elevator elevator;
    private Wrist wrist;

    private final double positionPIDConstant = 0; //TODO: Test ME!
    private final double rotationPIDConstant = 0; //TODO Test ME!
    public ScoreCoral(CoralConstant position, double levelHeight, double angle, SwerveDrive drive, Elevator elevator, Wrist wrist){
        this.position = position;
        this.levelHeight = levelHeight;
        this.angle = angle;

        this.drive = drive;
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);

    }

    public void run(Context context) {
        context.yield();

        AutoAlign launchedAutoAlign = new AutoAlign(new Pose2d(position.getX(), position.getZ()), drive).run(context);

        elevator.setPosition(levelHeight);
        wrist.setAngle(angle);


        while(!elevator.isAtPosition() || !wrist.isAtPosition() || Math.abs(launchedAutoAlign.getXPIDOutput()) < positionPIDConstant || Math.abs(launchedAutoAlign.getYPIDOutput()) < positionPIDConstant || Math.abs(launchedAutoAlign.getRotationPIDOutput()) < rotationPIDConstant){
            context.yield();
        }

        //Outtake
        

    }
    
}
