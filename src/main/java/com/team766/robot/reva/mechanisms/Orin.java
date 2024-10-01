package com.team766.robot.reva.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.orin.GetApriltagPoseData;
import com.team766.orin.TimestampedApriltag;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Orin extends MechanismWithStatus<Orin.OrinStatus> {
    public record OrinStatus(List<TimestampedApriltag> apriltags) implements Status {
        public Optional<TimestampedApriltag> getTagById(int id) {
            for (TimestampedApriltag tag : apriltags) {
                if (tag.tagId() == id) {
                    return Optional.of(tag);
                }
            }

            return Optional.empty();
        }
    }

    public Orin() {}

    @Override
    protected OrinStatus updateStatus() {
        var tags = GetApriltagPoseData.getAllTags(new double[0], 0);

        return new OrinStatus(Collections.unmodifiableList(tags));
    }
}
