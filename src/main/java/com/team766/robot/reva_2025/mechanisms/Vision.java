package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.orin.*;
import java.util.ArrayList;
import java.util.Optional;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {
    public record VisionStatus(ArrayList<ArrayList<TimestampedApriltag>> allTags)
            implements Status {
        public Optional<ArrayList<TimestampedApriltag>> getTagById(int id) {
            ArrayList<TimestampedApriltag> tagList = new ArrayList<>();
            for (ArrayList<TimestampedApriltag> cameraTags : allTags) {
                for (TimestampedApriltag tag : cameraTags) {
                    if (tag.ID == id) {
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
                    new GetOrinRawValue("cam199"),
                    new GetOrinRawValue("camUC762"),
                    new GetOrinRawValue("green"),
                    new GetOrinRawValue("blue")
                };
    }

    @Override
    protected VisionStatus updateStatus() {
        ArrayList<ArrayList<TimestampedApriltag>> tags = new ArrayList<>();
        for (GetOrinRawValue camera : cameraList) {
            try {
                double[] poseData = camera.getRawPoseData();
                tags.add(GetApriltagPoseData.getAllTags(poseData));
            } catch (ValueNotFoundOnTableError e) {
                // the outer tags list will be empty if no tags are seen, since no inner lists will
                // be added
                log(LoggerExceptionUtils.exceptionToString(e));
            }
        }
        log("Looped here!");
        return new VisionStatus(tags);
    }
}
