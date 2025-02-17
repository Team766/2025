package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.IntakeCoral;
import com.team766.robot.reva_2025.procedures.ScoreCoral;
import edu.wpi.first.math.geometry.Pose2d;

public class TestAuto extends PathSequenceAuto {
    public TestAuto(SwerveDrive drive, Elevator elevator, Wrist wrist, CoralIntake intake) {
        super(drive, new Pose2d());
        addPath("Inner Cage Start A to Reef 3");
        addProcedure(new ScoreCoral(RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef 3 to Coral Station 1");
        // intake
        addPath("Coral Station 1 to Reef 6");
        // addProcedure(new ScoreCoral(RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
        addPath("Reef 6 to Coral Station 1");
        // intake
        addPath("Coral Station 1 to Reef 7");

        //^^about 15 secs^^
    }
}
