package com.team766.orin;

import com.team766.robot.reva_2025.constants.FieldConstants;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public record TimestampedApriltag(double collectTime, int tagId, Pose3d pose3d) {
    public Translation2d toRobotPosition(Rotation2d gyro) {
        Translation2d tagPosition =
                (tagId == FieldConstants.TEST_TAG_ID)
                        ? FieldConstants.TEST_TAG_POSITION.getTranslation()
                        : FieldConstants.APRIL_TAG_REEFSCAPE_LAYOUT
                                .getTagPose(tagId)
                                .get()
                                .getTranslation()
                                .toTranslation2d();
        SmartDashboard.putNumber("Pose3d X", pose3d.getX());
        return tagPosition.minus(pose3d.getTranslation().toTranslation2d().rotateBy(gyro));
    }
}
