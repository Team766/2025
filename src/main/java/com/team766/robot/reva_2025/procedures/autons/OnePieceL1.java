package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.MoveElevator;
import com.team766.robot.reva_2025.procedures.RunCoralOut;
import com.team766.robot.reva_2025.procedures.StartCoralIntake;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class OnePieceL1 extends PathSequenceAuto {
    private CoralIntake intake;
    private Wrist wrist;
    private Elevator elevator;

    public OnePieceL1(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(drive, new Pose2d(7.608, 4.190, Rotation2d.fromDegrees(0)));
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.L1));
        addPath("Blue Line Start - L1 Score");
        addProcedure(new RunCoralOut(intake, 1));
    }
}
