package com.team766.robot.reva_2025.procedures.autons;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.gatorade.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class ThreePieceJLA extends PathSequenceAuto {
    public ThreePieceJLA(SwerveDrive drive, CoralIntake intake, Wrist wrist, Elevator elevator) {
        super(drive, new Pose2d(7.611, 6.18, Rotation2d.fromDegrees(-180)));
        addPath("Start Blue 2 - Reef J");
        addPath("Reef J - CoralStation 1");
        addPath("CoralStation 1 - Reef L");
        addPath("Reef L - CoralStation 1");
        addPath("CoralStation 1 - Reef A");
    }
}
