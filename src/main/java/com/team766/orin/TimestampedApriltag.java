package com.team766.orin;

import com.team766.robot.reva_2025.constants.FieldConstants;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public record TimestampedApriltag(double collectTime, int tagId, Pose3d pose) {
    public Translation2d toRobotPosition(Rotation2d gyro) {
        return FieldConstants.APRIL_TAG_REEFSCAPE_LAYOUT
                .getTagPose(this.tagId())
                .get()
                .getTranslation()
                .toTranslation2d()
                .minus(this.pose.getTranslation().toTranslation2d().rotateBy(gyro));
    }
}
