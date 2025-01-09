package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.orin.*;
import java.util.ArrayList;

public class Vision extends Mechanism {

    private GetOrinRawValue camera;

    public Vision(String ntCameraName) {
        camera = new GetOrinRawValue(ntCameraName);
    }

    public KalamanApriltag getTagById(int tagId) throws NoTagFoundError, ValueNotFoundOnTableError {
        double[] poseData;
        poseData = camera.getRawPoseData();

        ArrayList<KalamanApriltag> tags = GetApriltagPoseData.getAllTags(poseData);

        for (KalamanApriltag tag : tags) {
            if (tag.ID == tagId) return tag;
        }

        throw new NoTagFoundError(tagId);
    }

    public ArrayList<KalamanApriltag> getAllTags() throws ValueNotFoundOnTableError {
        double[] poseData;
        poseData = camera.getRawPoseData();

        ArrayList<KalamanApriltag> tags = GetApriltagPoseData.getAllTags(poseData);
        return tags;
    }
}
