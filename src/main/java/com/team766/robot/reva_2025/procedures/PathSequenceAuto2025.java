package com.team766.robot.reva_2025.procedures;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import edu.wpi.first.math.geometry.Pose2d;

public class PathSequenceAuto2025 extends PathSequenceAuto {
    private CoralIntake intake;
    private Wrist wrist;
    private Elevator elevator;

    public PathSequenceAuto2025(
            SwerveDrive drive,
            CoralIntake intake,
            Wrist wrist,
            Elevator elevator,
            Pose2d startPose) {
        super(drive, startPose);
        this.intake = reserve(intake);
        this.wrist = reserve(wrist);
        this.elevator = reserve(elevator);
    }
}
