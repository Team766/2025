package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.StartCoralIntake;
import com.team766.robot.reva_2025.procedures.MoveElevator;
import com.team766.robot.reva_2025.procedures.ScoreCoral;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class ThreePieceCDE extends PathSequenceAuto {
    public ThreePieceCDE(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(drive, new Pose2d(7.648, 0.772, Rotation2d.fromDegrees(-180)));
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.L4));
        addPath("Start Red 3 - Reef C");
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef C - Coral Station 2");
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.L4));
        addPath("Coral Station 2 - Reef D");
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef D - CoralStation 2");
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.L4));
        addPath("CoralStation 2 - Reef E");
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Right, ScoreHeight.L4, drive, elevator, wrist, intake));
    }
}
