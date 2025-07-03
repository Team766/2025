package com.team766.robot.copy_2910.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.orin.GetOrinRawValue;
import com.team766.orin.ValueNotFoundOnTableError;
import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {

        private static double leftScoringPositionX =
            ConfigFileReader.instance.getDouble("vision.leftScoringPositionX").valueOr(0d);

        private static double leftScoringPositionY =
            ConfigFileReader.instance.getDouble("vision.leftScoringPositionY").valueOr(0d);

        private static double rightScoringPositionX =
            ConfigFileReader.instance.getDouble("vision.rightScoringPositionX").valueOr(0d);

        private static double rightScoringPositionY =
            ConfigFileReader.instance.getDouble("vision.rightScoringPositionY").valueOr(0d);

        private GetOrinRawValue camera = new GetOrinRawValue("MainCamera", 0);

    public static record VisionStatus (int ID, double x, double y) implements Status {

        public Pose2d getApriltagPose2d(){

            return new Pose2d(x, y, new Rotation2d());
        }
    }

    public Vision() {
        
    }

    public static Pose2d getTargetPositionLeft(){
        double x = leftScoringPositionX;
        double y = leftScoringPositionY;
        Pose2d targetPosition = new Pose2d(x, y, new Rotation2d());
        return targetPosition;
    }

    public static Pose2d getTargetPositionRight(){
        double x = rightScoringPositionX;
        double y = rightScoringPositionY;
        Pose2d targetPosition = new Pose2d(x, y, new Rotation2d());
        return targetPosition;
    }



    @Override
    protected VisionStatus updateStatus() {
        double[] poseData;
        try {
            poseData = camera.getRawPoseData();
        } catch (ValueNotFoundOnTableError e) {
            log("No pose data found on table", e);
            return new VisionStatus(0, 0, 0); // Return a default status if no data is found
        }

        /*
         * Can assume there will be only one tag as the tag here should always be the one on the reef\
         * poseData[0] is the collect time - can ignore for this situation
         */
        //AprilTag tag = new AprilTag((int) poseData[1], new Pose3d(poseData[2], poseData[3], poseData[4], new Rotation3d()));
        return new VisionStatus((int) poseData[1], poseData[2], poseData[3]);

    }
    
}
