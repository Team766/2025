package com.team766.robot.reva_2025.procedures.autons;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.procedures.ScoreCoral;
import com.team766.robot.reva_2025.procedures.StartCoralIntake;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class OnePieceCenterG extends PathSequenceAuto {
    public OnePieceCenterG(
            SwerveDrive drive,
            CoralIntake intake,
            Wrist wrist,
            Elevator elevator,
            AlgaeIntake algaeIntake) {
        super(drive, new Pose2d(7.070, 4.229, Rotation2d.fromDegrees(-180)));
        // Score on J
        addProcedure(new StartCoralIntake(intake));
        addProcedure(
                new ScoreCoral(
                        RelativeReefPos.Left,
                        ScoreHeight.L4,
                        drive,
                        elevator,
                        wrist,
                        intake,
                        algaeIntake));
    }
}
