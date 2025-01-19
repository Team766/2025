package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.orin.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Vision extends MechanismWithStatus<Vision.VisionStatus> {
    public record VisionStatus(List<KalamanApriltag> apriltags) implements Status {
        public Optional<KalamanApriltag> getTagById(int id) {
            for (KalamanApriltag tag : apriltags) {
                if (tag.ID == id) {
                    return Optional.of(tag);
                }
            }

            return Optional.empty();
        }
    }

    private final GetOrinRawValue camera;

    public Vision(String ntCameraName) {
        camera = new GetOrinRawValue(ntCameraName);
    }

    @Override
    protected VisionStatus updateStatus() {
        double[] poseData;
        try {
            poseData = camera.getRawPoseData();
        } catch (ValueNotFoundOnTableError e) {
            poseData = new double[0];
        }

        ArrayList<KalamanApriltag> tags = GetApriltagPoseData.getAllTags(poseData);
        return new VisionStatus(Collections.unmodifiableList(tags));
    }
}
