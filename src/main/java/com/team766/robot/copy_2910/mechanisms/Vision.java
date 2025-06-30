package com.team766.robot.copy_2910.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.List;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {

    // TODO: Confirm if this is the correct field
    public static final AprilTagFieldLayout kTagLayout =
            AprilTagFieldLayout.loadField(AprilTagFields.k2025ReefscapeAndyMark);

    // TODO: Update with real extrinsics
    public static final Transform3d kRobotToCam =
            new Transform3d(new Translation3d(0, 0, 0), new Rotation3d(0, 0, 0));

    PhotonCamera camera = new PhotonCamera("Main2910Camera");

    Pose3d globalPose = new Pose3d();

    public static record VisionStatus(Pose3d pose) implements Status {

        public Pose3d getPose() {
            return pose;
        }
    }

    @Override
    protected VisionStatus updateStatus() {
        List<PhotonPipelineResult> res = camera.getAllUnreadResults();

        try {
            PhotonPipelineResult mostRecentResult = res.get(0);

            PhotonTrackedTarget bestTrackedTarget = mostRecentResult.getBestTarget();

            if (kTagLayout.getTagPose(bestTrackedTarget.getFiducialId()).isPresent()) {
                Pose3d robotPose =
                        PhotonUtils.estimateFieldToRobotAprilTag(
                                bestTrackedTarget.getBestCameraToTarget(),
                                kTagLayout.getTagPose(bestTrackedTarget.getFiducialId()).get(),
                                kRobotToCam);
                globalPose = robotPose;
            }
        } catch (Exception e) {
            // TODO: Add lights flagging for no tag seen
            log("No most recent pipeline result found");
        }

        return new VisionStatus(globalPose);
    }

    public void run() {}
}
