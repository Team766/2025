package com.team766.robot.reva_2025.procedures;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class TestAuto extends PathSequenceAuto {
    public TestAuto(SwerveDrive drive) {
        super(drive, new Pose2d(new Translation2d(7.539, 6.234), Rotation2d.fromDegrees(180)));
        addPath("Test Path 1");
    }
}
