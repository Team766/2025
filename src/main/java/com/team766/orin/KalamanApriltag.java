package com.team766.orin;

import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.math.geometry.Pose3d;

public class KalamanApriltag extends AprilTag {
    private final double collectTime;

    public KalamanApriltag(double collectTime, int tagId, Pose3d pose) {
        super(tagId, pose);
        this.collectTime = collectTime;
    }

    public double getCollectTime() {
        return collectTime;
    }
}
