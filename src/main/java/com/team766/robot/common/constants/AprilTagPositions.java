package com.team766.robot.common.constants;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import java.util.Map;

public final class AprilTagPositions {
    private AprilTagPositions() {}

    public static final Map<Integer, Pose2d> TAGS_2025 =
            Map.ofEntries(
                    Map.entry(1, new Pose2d(new Translation2d(657.37, 25.8), new Rotation2d())),
                    Map.entry(2, new Pose2d(new Translation2d(657.37, 291.2), new Rotation2d())),
                    Map.entry(3, new Pose2d(new Translation2d(455.15, 317.15), new Rotation2d())),
                    Map.entry(4, new Pose2d(new Translation2d(365.2, 241.64), new Rotation2d())),
                    Map.entry(5, new Pose2d(new Translation2d(365.2, 75.39), new Rotation2d())),
                    Map.entry(6, new Pose2d(new Translation2d(530.49, 130.17), new Rotation2d())),
                    Map.entry(7, new Pose2d(new Translation2d(546.87, 158.5), new Rotation2d())),
                    Map.entry(8, new Pose2d(new Translation2d(5, 5), new Rotation2d())),
                    Map.entry(9, new Pose2d(new Translation2d(497.77, 186.83), new Rotation2d())),
                    Map.entry(10, new Pose2d(new Translation2d(481.39, 158.5), new Rotation2d())),
                    Map.entry(11, new Pose2d(new Translation2d(0, 0), new Rotation2d())),
                    Map.entry(12, new Pose2d(new Translation2d(33.51, 25.8), new Rotation2d())),
                    Map.entry(13, new Pose2d(new Translation2d(33.51, 291.2), new Rotation2d())),
                    Map.entry(14, new Pose2d(new Translation2d(325.68, 241.64), new Rotation2d())),
                    Map.entry(15, new Pose2d(new Translation2d(5, 5), new Rotation2d())),
                    Map.entry(16, new Pose2d(new Translation2d(8, 8), new Rotation2d())),
                    Map.entry(17, new Pose2d(new Translation2d(160.39, 130.17), new Rotation2d())),
                    Map.entry(18, new Pose2d(new Translation2d(144.0, 158.5), new Rotation2d())),
                    Map.entry(19, new Pose2d(new Translation2d(160.39, 186.83), new Rotation2d())),
                    Map.entry(20, new Pose2d(new Translation2d(193.1, 186.83), new Rotation2d())),
                    Map.entry(21, new Pose2d(new Translation2d(209.49, 158.5), new Rotation2d())),
                    Map.entry(22, new Pose2d(new Translation2d(193.1, 130.17), new Rotation2d())));
}
