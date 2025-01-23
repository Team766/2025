package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.orin.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {
    public record VisionStatus(List<TimestampedApriltag> apriltags) implements Status {
        public Optional<TimestampedApriltag> getTagById(int id) {
            for (TimestampedApriltag tag : apriltags) {
                if (tag.ID == id) {
                    return Optional.of(tag);
                }
            }

            return Optional.empty();
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
        ArrayList<Double> poseData = new ArrayList<>();
        try {
            for (GetOrinRawValue camera : cameraList) {
                for (double data : camera.getRawPoseData()) {
                    poseData.add(data);
                }
            }
        } catch (ValueNotFoundOnTableError e) {
            log(LoggerExceptionUtils.exceptionToString(e));
        }

        ArrayList<TimestampedApriltag> tags = GetApriltagPoseData.getAllTags(poseData);
        log("Looped here!");
        return new VisionStatus(tags);
    }
}
