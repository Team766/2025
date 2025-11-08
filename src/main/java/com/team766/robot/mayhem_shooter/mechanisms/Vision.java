package com.team766.robot.mayhem_shooter.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.orin.GetApriltagPoseData;
import com.team766.orin.GetOrinRawValue;
import com.team766.orin.TimestampedApriltag;
import com.team766.orin.ValueNotFoundOnTableError;
import java.util.List;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {

    public static record VisionStatus() implements Status {}

    private GetOrinRawValue frontCamera = new GetOrinRawValue("199", 0);
    private GetOrinRawValue backCamera = new GetOrinRawValue("12", 0);

    private final double slope = 0.0897;
    private final double intercept = 0.254;

    // Power (percentage) = slope * distance (meters) + intercept

    public Vision() {}

    public double getDistanceFromTagFront(int tagID) {
        try {
            double[] poseData = frontCamera.getRawPoseData();
            List<TimestampedApriltag> tags = GetApriltagPoseData.getAllTags(poseData, 0);
            for (TimestampedApriltag tag : tags) {
                if (tag.tagId() == tagID) {
                    return Math.sqrt(
                            Math.pow(tag.pose3d().getX(), 2)
                                    + Math.pow(tag.pose3d().getY(), 2)
                                    + Math.pow(tag.pose3d().getZ(), 2));
                }
            }
        } catch (ValueNotFoundOnTableError e) {
            log(e.toString());
            return 0;
        }
        return 0;
    }

    public double getDistanceFromTagBack(int tagID) {
        try {
            double[] poseData = backCamera.getRawPoseData();
            List<TimestampedApriltag> tags = GetApriltagPoseData.getAllTags(poseData, 0);
            for (TimestampedApriltag tag : tags) {
                if (tag.tagId() == tagID) {
                    return Math.sqrt(
                            Math.pow(tag.pose3d().getX(), 2)
                                    + Math.pow(tag.pose3d().getY(), 2)
                                    + Math.pow(tag.pose3d().getZ(), 2));
                }
            }
        } catch (ValueNotFoundOnTableError e) {
            log(e.toString());
            return 0;
        }
        return 0;
    }

    public void sendLog() {
        // log("Back to 1: " + getDistanceFromTagBack(1));
        log("here");
        log("Front to 2: " + getDistanceFromTagFront(1));
    }

    public double getShooterSpeedFromDistance() {
        double distance;
        try {
            distance = getDistanceFromTagFront(1);

            if (distance == 0) {
                distance = getDistanceFromTagFront(3);
            }
        } catch (Exception e) {
            return 0;
        }

        return slope * distance + intercept;
    }

    @Override
    protected VisionStatus updateStatus() {
        return new VisionStatus();
    }
}
