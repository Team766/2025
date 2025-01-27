package com.team766.orin;

import com.team766.robot.reva_2025.constants.FieldConstants;
import edu.wpi.first.apriltag.AprilTag;
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
        return FieldConstants.APRIL_TAG_REEFSCAPE_LAYOUT
                .getTagPose(this.ID)
                .get()
                .getTranslation()
                .toTranslation2d()
                .minus(this.pose.getTranslation().toTranslation2d().rotateBy(gyro));
    }
}
