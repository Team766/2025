package com.team766.robot.reva_2025.constants;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;

public class FieldConstants {
    // utility class
    private FieldConstants() {}

    public static final AprilTagFieldLayout APRIL_TAG_REEFSCAPE_LAYOUT =
            AprilTagFieldLayout.loadField(AprilTagFields.k2025Reefscape);

    public static final double TEST_TAG_ID = 254;
    public static final Pose2d TEST_TAG_POSITION = new Pose2d();
}
