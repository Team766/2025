package com.team766.robot.reva_2025.procedures.autons;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.ScoreCoral;
import com.team766.robot.reva_2025.procedures.CoralStationPositionAndIntake;
import com.team766.robot.reva_2025.procedures.PathSequenceAuto2025;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class ThreePieceJLA extends PathSequenceAuto2025 {
    public ThreePieceJLA(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(drive, intake, wrist, elevator, new Pose2d(7.611, 6.18, Rotation2d.fromDegrees(-180)));
        addPath("Start Blue 2 - Reef J");
        addProcedure(new ScoreCoral(RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef J - CoralStation 1");
        addProcedure(new CoralStationPositionAndIntake(elevator, wrist, intake));
        addPath("CoralStation 1 - Reef L");
        addProcedure(new ScoreCoral(RelativeReefPos.Right, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef L - CoralStation 1");
        addPath("CoralStation 1 - Reef A");
        addProcedure(new ScoreCoral(RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
    }
}
