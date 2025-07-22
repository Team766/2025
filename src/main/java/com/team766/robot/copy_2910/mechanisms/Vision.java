package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.orin.GetOrinRawValue;
import com.team766.orin.ValueNotFoundOnTableError;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {

    private static double L2L3X = 0.5597;

    private static double L4X = 0.4533;

    private static double leftScoringPositionY = -0.0962;

    private static double rightScoringPositionY = 0.200;

    private GetOrinRawValue camera = new GetOrinRawValue("766", 0);

    public static record VisionStatus(int ID, double x, double y, boolean isValid)
            implements Status {

        public Pose2d getApriltagPose2d() throws Exception {

            if (!isValid) {
                throw new Exception("Pose is not valid");
            }
            return new Pose2d(x, y, new Rotation2d());
        }
    }

    public Vision() {}

    public static Pose2d getTargetPositionLeftL2L3() {
        return new Pose2d(L2L3X, leftScoringPositionY, new Rotation2d());
    }

    public static Pose2d getTargetPositionRightL2L3() {
        return new Pose2d(L2L3X, rightScoringPositionY, new Rotation2d());
    }

    public static Pose2d getTargetPositionLeftL4() {
        return new Pose2d(L4X, leftScoringPositionY, new Rotation2d());
    }

    public static Pose2d getTargetPositionRightL4() {
        return new Pose2d(L4X, rightScoringPositionY, new Rotation2d());
    }

    @Override
    protected VisionStatus updateStatus() {
        double[] poseData;
        try {
            poseData = camera.getRawPoseData();
        } catch (ValueNotFoundOnTableError e) {
            log("No pose data found on table for camera: " + e.getMessage());
            return new VisionStatus(0, 0, 0, false); // Return a default status if no data is found
        }

        /*
         * Can assume there will be only one tag as the tag here should always be the one on the reef\
         * poseData[0] is the collect time - can ignore for this situation
         */
        // AprilTag tag = new AprilTag((int) poseData[1], new Pose3d(poseData[2], poseData[3],
        // poseData[4], new Rotation3d()));
        if (Math.sqrt(poseData[2] * poseData[2] + poseData[3] * poseData[3]) > 2.0) {
            // log(
            //        "Closest tag is over two meters away. Move closer to the tag, as this is
            // probably not a valid reef tag.");
            return new VisionStatus((int) poseData[1], 0, 0, false); // Return invalid status
        }
        return new VisionStatus((int) poseData[1], poseData[2], poseData[3], true);
    }
}
