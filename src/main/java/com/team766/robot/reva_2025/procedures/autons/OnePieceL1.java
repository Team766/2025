package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.IntakeCoralUntilIn;
import com.team766.robot.reva_2025.procedures.PathSequenceAuto2025;
import com.team766.robot.reva_2025.procedures.RunCoralOut;
import com.team766.robot.reva_2025.procedures.ScoreCoral;
import com.team766.robot.reva_2025.procedures.StartCoralIntake;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class OnePieceL1 extends PathSequenceAuto2025 {
    private CoralIntake intake;
    private Wrist wrist;
    private Elevator elevator;

    public OnePieceL1(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(
                drive,
                intake,
                wrist,
                elevator,
                new Pose2d(7.608, 4.190, Rotation2d.fromDegrees(0)));
        addProcedure(new IntakeCoralUntilIn(intake));
        addPath("Blue Line Start - L1 Score");
        addProcedure(
                new RunCoralOut(intake, 1));
    }
}
