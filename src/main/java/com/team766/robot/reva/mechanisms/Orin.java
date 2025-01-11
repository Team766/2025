package com.team766.robot.reva.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.orin.GetApriltagPoseData;
import com.team766.orin.NoTagFoundError;
import com.team766.robot.reva.Robot;
import edu.wpi.first.apriltag.AprilTag;

public class Orin extends Mechanism {
    public Orin() {}

    public AprilTag getTagById(int id) throws NoTagFoundError {
        var tags = GetApriltagPoseData.getAllTags(new double[0]);

        for (AprilTag tag : tags) {
            if (tag.ID == id) return tag;
        }

        throw new NoTagFoundError(id);
    }

    public void run() {
        var tags = GetApriltagPoseData.getAllTags(new double[0]);

        if (tags.size() > 0) {
            Robot.lights.signalHasTag();
        }
    }
}
