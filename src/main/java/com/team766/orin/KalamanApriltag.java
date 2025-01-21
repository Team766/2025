package com.team766.orin;

import com.team766.robot.common.constants.AprilTagPositions;
import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class KalamanApriltag extends AprilTag {
    private double collectTime = -1;

    public KalamanApriltag(double collectTime, int tagId, Pose3d pose) {
        super(tagId, pose);
        this.collectTime = collectTime;
    }

    public double getCollectTime() {
        return collectTime;
    }

    public Translation2d toRobotPosition(Rotation2d gyro) {
        return AprilTagPositions.TAGS_2025
                .get(this.ID)
                .getTranslation()
                .rotateBy(gyro.times(-1.0))
                .minus(this.pose.getTranslation().toTranslation2d());
    }
}
