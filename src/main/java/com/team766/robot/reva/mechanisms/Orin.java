package com.team766.robot.reva.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.orin.GetApriltagPoseData;
import com.team766.orin.KalamanApriltag;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Orin extends MechanismWithStatus<Orin.OrinStatus> {
    public record OrinStatus(List<KalamanApriltag> apriltags) implements Status {
        public Optional<KalamanApriltag> getTagById(int id) {
            for (KalamanApriltag tag : apriltags) {
                if (tag.ID == id) {
                    return Optional.of(tag);
                }
            }

            return Optional.empty();
        }
    }

    public Orin() {}

    @Override
    protected OrinStatus updateStatus() {
        var tags = GetApriltagPoseData.getAllTags(new double[0]);

        return new OrinStatus(Collections.unmodifiableList(tags));
    }
}
