package com.team766.robot.jackrabbit.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import edu.wpi.first.net.PortForwarder;
import frc.robot.LimelightHelpers;
import java.util.Optional;

public class NearsightedLimelight
        extends MechanismWithStatus<NearsightedLimelight.NearsightStatus> {
    public record Observation(/*TODO: double timestamp,*/ double x, double y) {}

    public record NearsightStatus(Optional<Observation> observation) implements Status {}

    private static final String LIMELIGHT_NAME = "nearsighted";

    public NearsightedLimelight() {
        // (robotIP):5801 will now point to a Limelight3A's (id 0) web interface stream:
        // (robotIP):5800 will now point to a Limelight3A's (id 0) video stream:
        PortForwarder.add(5801, "172.29.0.1", 5801);
        PortForwarder.add(5802, "172.29.0.1", 5802);
        PortForwarder.add(5803, "172.29.0.1", 5803);
        PortForwarder.add(5804, "172.29.0.1", 5804);
        PortForwarder.add(5805, "172.29.0.1", 5805);
        PortForwarder.add(5806, "172.29.0.1", 5806);
        PortForwarder.add(5807, "172.29.0.1", 5807);
        PortForwarder.add(5808, "172.29.0.1", 5808);
        PortForwarder.add(5809, "172.29.0.1", 5809);
    }

    @Override
    protected NearsightStatus updateStatus() {
        Optional<Observation> observation;
        if (LimelightHelpers.getTV(LIMELIGHT_NAME)) {
            observation =
                    Optional.of(
                            new Observation(
                                    LimelightHelpers.getTX(LIMELIGHT_NAME),
                                    LimelightHelpers.getTY(LIMELIGHT_NAME)));
        } else {
            observation = Optional.empty();
        }
        return new NearsightStatus(observation);
    }
}
