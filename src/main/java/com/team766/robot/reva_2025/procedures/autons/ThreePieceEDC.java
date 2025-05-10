package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.IntakeCoralUntilIn;
import com.team766.robot.reva_2025.procedures.MoveElevator;
import com.team766.robot.reva_2025.procedures.ScoreCoral;
import com.team766.robot.reva_2025.procedures.StartCoralIntake;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class ThreePieceEDC extends PathSequenceAuto {
    public ThreePieceEDC(
            SwerveDrive drive,
            CoralIntake intake,
            Wrist wrist,
            Elevator elevator,
            AlgaeIntake algaeIntake) {
        super(drive, new Pose2d(7.100, 2.454, Rotation2d.fromDegrees(-180)));
        // Score on E
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.L4));
        addPath("Start Red 1 - Reef E");

        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Left,
                        ScoreHeight.L4,
                        drive,
                        elevator,
                        wrist,
                        intake,
                        algaeIntake));

        // Intake
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.Intake));
        addPath("Reef E - Coral Station 2");
        addProcedure(new IntakeCoralUntilIn(intake));

        // Score on C
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Left,
                        ScoreHeight.L4,
                        drive,
                        elevator,
                        wrist,
                        intake,
                        algaeIntake));

        // Intake
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.Intake));
        addPath("Reef C - Coral Station 2");
        addProcedure(new IntakeCoralUntilIn(intake));

        // Score on D
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Right,
                        ScoreHeight.L4,
                        drive,
                        elevator,
                        wrist,
                        intake,
                        algaeIntake));

        // Intake
        addProcedure(new StartCoralIntake(intake));
        addProcedure(new MoveElevator(elevator, wrist, ScoreHeight.Intake));
        addPath("Reef D - Coral Station 2");
        addProcedure(new IntakeCoralUntilIn(intake));
    }
}
