package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.orin.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {
    public record VisionStatus(List<List<TimestampedApriltag>> allTags) implements Status {
        public Optional<List<TimestampedApriltag>> getTagById(int id) {
            List<TimestampedApriltag> tagList = new ArrayList<>();
            for (List<TimestampedApriltag> cameraTags : allTags) {
                for (TimestampedApriltag tag : cameraTags) {
                    if (tag.tagId() == id) {
                        tagList.add(tag);
                    }
                }
            }

            return tagList.isEmpty() ? Optional.empty() : Optional.of(tagList);
        }
    }

    private final GetOrinRawValue[] cameraList;

    public Vision() {
        // TODO: have this as a config input

        cameraList =
                new GetOrinRawValue[] {
                    new GetOrinRawValue("camUC762"),
                    new GetOrinRawValue("cam199"),
                    new GetOrinRawValue("green"),
                    new GetOrinRawValue("blue")
                };
    }

    @Override
    protected VisionStatus updateStatus() {
        List<List<TimestampedApriltag>> tags = new ArrayList<>();
        for (GetOrinRawValue camera : cameraList) {
            try {
                double[] poseData = camera.getRawPoseData();
                tags.add(GetApriltagPoseData.getAllTags(poseData));
            } catch (ValueNotFoundOnTableError e) {
                // maintain camera list order even if one is not connected
                tags.add(new ArrayList<>());
                log(Severity.ERROR, LoggerExceptionUtils.exceptionToString(e));
            }
        }
        return new VisionStatus(tags);
    }
}
