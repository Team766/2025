package com.team766.orin;

import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class TimestampedApriltag extends AprilTag {
    private final double collectTime;

    public TimestampedApriltag(double collectTime, int tagId, Pose3d pose) {
        super(tagId, pose);
        this.collectTime = collectTime;
    }

    public double getCollectTime() {
        return collectTime;
    }

    public Translation2d toRobotPosition(Rotation2d gyro) {
        return AprilTagFieldLayout.loadField(AprilTagFields.k2025Reefscape)
                .getTagPose(this.ID)
                .get()
                .getTranslation()
                .toTranslation2d()
                .minus(this.pose.getTranslation().toTranslation2d().rotateBy(gyro));
    }
}
