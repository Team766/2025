package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import com.team766.orin.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private boolean hasLoggedError = false;
    private final List<Integer> IGNORED_TAGS = Arrays.asList(4, 5, 14, 15);

    public Vision() {
        // TODO: have this as a config input

        cameraList =
                new GetOrinRawValue[] {
                    // disabling cameras for now
                    new GetOrinRawValue("left_back", 0.0009),
                    // new GetOrinRawValue("left_front", 0.0049),
                    // new GetOrinRawValue("right_back", 0.0009),
                    // new GetOrinRawValue("right_front", 0.0049)
                };
    }

    @Override
    protected VisionStatus updateStatus() {
        List<List<TimestampedApriltag>> tags = new ArrayList<>();
        for (GetOrinRawValue camera : cameraList) {
            try {
                double[] poseData = camera.getRawPoseData();
                List<TimestampedApriltag> tagData =
                        GetApriltagPoseData.getAllTags(poseData, camera.getCovariance());
                tagData.removeIf(tag -> IGNORED_TAGS.contains(tag.tagId()));
                tags.add(tagData);
            } catch (ValueNotFoundOnTableError e) {
                // maintain camera list order even if one is not connected
                tags.add(new ArrayList<>());
                if (!hasLoggedError) {
                    log(Severity.ERROR, LoggerExceptionUtils.exceptionToString(e));
                    hasLoggedError = true;
                }
            }
        }
        return new VisionStatus(tags);
    }
}
