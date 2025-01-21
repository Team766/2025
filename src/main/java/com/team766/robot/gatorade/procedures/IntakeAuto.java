package com.team766.robot.gatorade.procedures;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.gatorade.GamePieceType;
import com.team766.robot.gatorade.mechanisms.Intake;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class IntakeAuto extends PathSequenceAuto {
    public IntakeAuto(GamePieceType gamePieceType, SwerveDrive drive, Intake intake) {
        super(drive, new Pose2d(2.00, 7.00, new Rotation2d(0)));
        addProcedure(new IntakeIn(gamePieceType, intake));
        addPath("Intake_Path_1");
        addProcedure(new IntakeIdle(gamePieceType, intake));
        addPath("Intake_Path_2");
        addProcedure(new IntakeOut(gamePieceType, intake));
        addProcedure(new StopDrive(drive));
        addWait(1);
        addProcedure(new IntakeStop(intake));
        addPath("Intake_Path_3");
        addProcedure(new IntakeIn(gamePieceType, intake));
        addPath("Intake_Path_4");
        addProcedure(new StopDrive(drive));
        addProcedure(new IntakeOut(gamePieceType, intake));
        addWait(2);
        addProcedure(new IntakeStop(intake));
    }
}
