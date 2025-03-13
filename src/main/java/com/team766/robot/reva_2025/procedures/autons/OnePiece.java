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
import com.team766.robot.reva_2025.procedures.IntakeCoralUntilIn;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class OnePiece extends PathSequenceAuto {
    private CoralIntake intake;
    private Wrist wrist;
    private Elevator elevator;

    public OnePiece(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(drive, new Pose2d(7.608, 4.190, Rotation2d.fromDegrees(-180)));
        this.intake = reserve(intake);
        this.wrist = reserve(wrist);
        this.elevator = reserve(elevator);
        addProcedure(new IntakeCoralUntilIn(intake));
        addPath("Simple");
        addProcedure(new ScoreCoral(RelativeReefPos.Left, ScoreHeight.L4, drive, elevator, wrist, intake));
    }
}