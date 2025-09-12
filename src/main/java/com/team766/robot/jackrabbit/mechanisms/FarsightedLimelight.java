package com.team766.robot.jackrabbit.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.framework.StatusBus;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.net.PortForwarder;
import frc.robot.LimelightHelpers;
import java.util.Optional;

public class FarsightedLimelight extends MechanismWithStatus<FarsightedLimelight.FarsightStatus> {
    public record PoseEstimate(double timestamp, Pose2d pose) {}

    public record FarsightStatus(Optional<PoseEstimate> poseEstimate) implements Status {}

    public FarsightedLimelight() {
        // (robotIP):5811 will now point to a Limelight3A's (id 1) web interface stream:
        // (robotIP):5810 will now point to a Limelight3A's (id 1) video stream:
        PortForwarder.add(5811, "172.29.1.1", 5801);
        PortForwarder.add(5812, "172.29.1.1", 5802);
        PortForwarder.add(5813, "172.29.1.1", 5803);
        PortForwarder.add(5814, "172.29.1.1", 5804);
        PortForwarder.add(5815, "172.29.1.1", 5805);
        PortForwarder.add(5816, "172.29.1.1", 5806);
        PortForwarder.add(5817, "172.29.1.1", 5807);
        PortForwarder.add(5818, "172.29.1.1", 5808);
        PortForwarder.add(5819, "172.29.1.1", 5809);
    }

    @Override
    protected FarsightStatus updateStatus() {
        boolean poseValid = true;

        var driveStatus = StatusBus.getInstance().getStatus(Drive.DriveStatus.class);
        if (driveStatus.isPresent()) {
            var s = driveStatus.get();
            LimelightHelpers.SetRobotOrientation(
                    "limelight", s.heading(), s.yawRate(), s.pitch(), 0, s.roll(), 0);
            if (Math.abs(s.yawRate())
                    > 720) { // if our angular velocity is greater than 720 degrees per second,
                // ignore vision updates
                poseValid = false;
            }
        }

        Optional<PoseEstimate> poseEstimate;
        LimelightHelpers.PoseEstimate mt2 =
                LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2("limelight");
        if (mt2.tagCount == 0) {
            poseValid = false;
        }
        if (poseValid) {
            poseEstimate = Optional.of(new PoseEstimate(mt2.timestampSeconds, mt2.pose));
        } else {
            poseEstimate = Optional.empty();
        }

        return new FarsightStatus(poseEstimate);
    }
}
